/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2011 - 2018 Henix
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.ta.plugin.db.assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbComparisonFailure;
import org.dbunit.assertion.Difference;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.SortedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.exception.BinaryAssertionFailedException;
import org.squashtest.ta.framework.exception.IllegalConfigurationException;
import org.squashtest.ta.framework.exception.TestAssertionFailure;
import org.squashtest.ta.plugin.commons.helpers.DiffReportBuilder;
import org.squashtest.ta.plugin.db.library.dbunit.PPKFilter;
import org.squashtest.ta.plugin.db.library.dbunit.assertion.DbUnitAssertExtension;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;
import org.squashtest.ta.plugin.db.resources.DbUnitFilterResource;
import org.squashtest.ta.plugin.db.resources.DbUnitPPKFilter;

public abstract class AbstractDbUnitDatasetCompare {

	protected static final Column[] NO_PK_VALUE = new Column[0];
	protected static final String DIFF_RESOURCE_NAME = "diff";
	
	protected static final String FIRST = "first";
	protected static final String SECOND = "second";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDbUnitDatasetCompare.class);
	
	protected DbUnitDatasetResource expected;
	protected DbUnitDatasetResource actual;
	
	protected DbUnitPPKFilter pseudoPrimaryKeys;
	
	/** Filters to apply before comparison. */
	private List<DbUnitFilterResource> filters = new ArrayList<DbUnitFilterResource>();

	protected AssertConnector assertConnector = new AssertConnector();
	
	protected abstract void compare(IDataSet pExpected, IDataSet pActual);
	
	protected DiffReportBuilderFactory diffReportBuilderFactory = new DiffReportBuilderFactory();

	public DiffReportBuilderFactory getDiffReportBuilderFactory() {
		return diffReportBuilderFactory;
	}

	public void setDiffReportBuilderFactory(DiffReportBuilderFactory diffReportBuilderFactory) {
		this.diffReportBuilderFactory = diffReportBuilderFactory;
	}

	public void setActualResult(DbUnitDatasetResource actual) {
		this.actual = actual;
	}

	public void setExpectedResult(DbUnitDatasetResource expected) {
		this.expected = expected;
	}

	/**
	 * This assertion needs no configuration, but will apply any injected
	 * {@link DbUnitFilterResource} to both the expected and actual dataset
	 * before comparing them.
	 */
	public void addConfiguration(Collection<Resource<?>> configuration) {
		for (Resource<?> confElement : configuration) {
			if (confElement instanceof DbUnitFilterResource) {
				filters.add((DbUnitFilterResource) confElement);
			} else if (confElement instanceof DbUnitPPKFilter) {
				if (pseudoPrimaryKeys == null) {
					pseudoPrimaryKeys = (DbUnitPPKFilter) confElement;
				} else {
					LOGGER.warn("Redundant DbUnitPPKFilter configuration will be ignored.");
				}
			} else {
				LOGGER.warn("Unrecognized configuration resource will be ignored (type: "
						+ confElement.getClass().getName() + ")");
			}
		}
	}

	/**
	 * @see BinaryAssertion#test()
	 * @throws BinaryAssertionFailedException if the asserted condition is false.
	 */
	public void test() throws BinaryAssertionFailedException {
			// extract datasets
			IDataSet expectedDataset;
			IDataSet actualDataset;
			expectedDataset = expected.getDataset();
			actualDataset = actual.getDataset();
	
			// apply filters, if any
			if (filters.size() > 0) {
				for (DbUnitFilterResource filter : filters) {
					expectedDataset = filter.apply(expectedDataset);
					actualDataset = filter.apply(actualDataset);
				}
			}
			compare(expectedDataset, actualDataset);
	}

	protected void logFailureReportingError(Exception e) {
		LOGGER.error(
				"Error while reporting assertion failure. Failure details won't be available.",
				e);
	}
	
	
	/********************************* Other class *****************************/
	
	/** package accessible for testability */
	class AssertConnector {
		public void assertEquals(SortedTable expected, SortedTable actual,
				FailureHandler failureHandler) throws DatabaseUnitException {
			try{
				Assertion.assertEquals(expected, actual, failureHandler);
			}catch(AssertionError err){
				throw new TestAssertionFailure(err.getMessage(), err);
			}
		}
		
		public void assertContains(ITable expected, ITable actual, FailureHandler failureHandler, List<String> primaryKeysName )throws DatabaseUnitException
		{
			try{
				DbUnitAssertExtension assertion = new DbUnitAssertExtension();
				assertion.assertContains(expected, actual, failureHandler, primaryKeysName);
			}catch (DbComparisonFailure dbcf) {
				throw new BinaryAssertionFailedException("Dataset comparison threw dbunit error : "+dbcf.getMessage(),AbstractDbUnitDatasetCompare.this.expected,AbstractDbUnitDatasetCompare.this.actual,null);
			}catch(AssertionError err){
				throw new TestAssertionFailure(err.getMessage(), err);
			}
		}
		
	}
	
	/**
	 * This method tries to compute the set of PK(s) for a given dataset table.
	 * 
	 * @param pkMap
	 *            cache to reuse results from previous diff lines
	 * @param table
	 *            the target table
	 * @return an array holding the table primary key column definitions if the
	 *         information was available. If no primary key information exists,
	 *         an empty array is cached and returned to speed up the next look
	 *         up.
	 * @throws DataSetException
	 *             if some error occurs while probing the dataset.
	 */
	protected Column[] extractPrimaryKeys(Map<String, Column[]> pkMap,
			ITable table) throws DataSetException {
		
		// first, try from Map to use cache from previous iteration
		ITableMetaData tableMetaData = table.getTableMetaData();
		String tableName = tableMetaData.getTableName();
		Column[] primaryKeys = NO_PK_VALUE;
		
		//then, check if there is ppk
		if (hasPpk(tableName)) {
			primaryKeys = extractPseudoPrimaryKeys(tableMetaData, tableMetaData);
		}else{
			primaryKeys = pkMap.get(tableName);
			// if cache miss, try from dataset
			if (primaryKeys == null) {
				primaryKeys = tableMetaData.getPrimaryKeys();
			}
		}
		return primaryKeys;
	}
	
	/**
	 * This method tries to compute the set of PK(s) for a given dataset table.
	 * 
	 * @param pExpected
	 *            the expected table
	 * @param pActual
	 *            the actual table
	 * @return an array holding the table primary key column definitions if the
	 *         information was available. If no primary key information exists,
	 *         an empty array is cached and returned to speed up the next look
	 *         up.
	 * @throws DataSetException
	 *             if some error occurs while probing the dataset.
	 */
	protected Column[] extractPrimaryKeys(ITableMetaData pExpected,
			ITableMetaData pActual) throws DataSetException {

		ITableMetaData tableMetaData = pExpected;
		String tableName = tableMetaData.getTableName();
		Column[] pkColumn = NO_PK_VALUE;
		
		if (hasPpk(tableName)) {
			pkColumn = extractPseudoPrimaryKeys(pActual, tableMetaData);
		} else {
			pkColumn = tableMetaData.getPrimaryKeys();
			if (pkColumn == null || pkColumn.length == 0) {
				List<String> expectedColumn = getColumnName(tableMetaData
						.getColumns());
				tableMetaData = pActual;
				List<Column> actualPrimaryKeys = new ArrayList<Column>();
				for (Column actualColumn : tableMetaData.getPrimaryKeys()) {
					if (expectedColumn.contains(actualColumn.getColumnName())) {
						actualPrimaryKeys.add(actualColumn);
					}
				}
				pkColumn = actualPrimaryKeys.toArray(new Column[actualPrimaryKeys.size()]);
			}
		}
		if (pkColumn == null) {
			pkColumn = NO_PK_VALUE;
		}
		return pkColumn;
	}
	
	private boolean hasPpk(String tableName){
		return pseudoPrimaryKeys != null && pseudoPrimaryKeys.getFilter().hasPpk(tableName);
	}
	
	private Column[] extractPseudoPrimaryKeys(ITableMetaData pActual, ITableMetaData pExpected) throws DataSetException{
		PPKFilter.ValidationResult validationResult = pseudoPrimaryKeys.getFilter().validPpkDefinition(pExpected);
		if (validationResult.isValid()) {
			return validationResult.getMatchingColumn();
		} else {
			throw new IllegalConfigurationException(validationResult.getNotFoundColumnAsString(pActual));
		}
	}
	
	protected List<String> getColumnName(Column[] columnArray) {
		List<String> listColumnName = new ArrayList<String>();
		for (Column column : columnArray) {
			listColumnName.add(column.getColumnName());
		}
		return listColumnName;
	}
	
	/**
	 * Get the pk for each differenced rows
	 * @param diff
	 * 				the Difference found
	 * @return a map with names and values of pk columns
	 * @throws DataSetException
	 */
	protected Map<String, String> getPkSet(Difference diff) throws DataSetException{
		Map<String, Column[]> pkMap = new HashMap<String, Column[]>();
		Column[] primaryKeys = extractPrimaryKeys(pkMap, diff.getExpectedTable());
		Map<String, String> pkSet = null;
		if (primaryKeys != null) {
			//if the expected Table does not have primary keys, we try with the actual table
			if (primaryKeys.length == 0) {
				primaryKeys = extractPrimaryKeys(pkMap, diff.getActualTable());
			}
			pkSet = new HashMap<String, String>(
					primaryKeys.length);
			for (Column c : primaryKeys) {
				Object valueObject = diff.getExpectedTable()
						.getValue(diff.getRowIndex(), c.getColumnName());
				String valueString = valueObject.toString();
				pkSet.put(c.getColumnName(), valueString);
			}
		}
		return pkSet;
	}
	
	/**
	 * Accessible mainly for testability purpose. May be used to change
	 * reporting behavior.
	 */
	public static class DiffReportBuilderFactory {
		public DiffReportBuilder newInstance() {
			return new DiffReportBuilder();
		}
	}
}