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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DiffCollectingFailureHandler;
import org.dbunit.assertion.Difference;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.SortedTable;
import org.squashtest.ta.core.tools.io.BinaryData;
import org.squashtest.ta.framework.annotations.TABinaryAssertion;
import org.squashtest.ta.framework.components.BinaryAssertion;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.framework.exception.BinaryAssertionFailedException;
import org.squashtest.ta.framework.exception.TestAssertionFailure;
import org.squashtest.ta.framework.test.result.ResourceAndContext;
import org.squashtest.ta.framework.tools.TempDir;
import org.squashtest.ta.plugin.commons.helpers.DiffReportBuilder;
import org.squashtest.ta.plugin.commons.helpers.ExecutionReportResourceMetadata;
import org.squashtest.ta.plugin.db.library.dbunit.helper.LowerCasedTable;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;

/**
 * Binary assertion that checks that effective dataset contains expected
 * dataset.
 * 
 * @author edegenetais
 * 
 */

@TABinaryAssertion("equal")
public class DbUnitDatasetEquals extends AbstractDbUnitDatasetCompare implements
		BinaryAssertion<DbUnitDatasetResource, DbUnitDatasetResource> {

	private static final String ERROR_MESSAGE = "The two compared datasets are different.";

	private static final String VARIABLE_1 = "#ONE#";
	private static final String VARIABLE_2 = "#TWO#";
	
	
	@Override
	protected void compare(final IDataSet pExpected, final IDataSet pActual) {
		try {
			
			//we first check that the two tables have as much tables, and that they have the same name.
			checkTables(pExpected, pActual);
			//then we check that each table have the same number of columns and rows.
			checkColumnsNumber(pExpected, pActual);
			checkRowsNumber(pExpected, pActual);
			
			//if all that is correct, we compared rows as in DbUnitDatasetContains
			compareRows(pExpected, pActual);

		} catch (DataSetException bde) {
			throw new BinaryAssertionFailedException(ERROR_MESSAGE,	expected, actual, buildFailReport(bde.getMessage()));
		} catch (DatabaseUnitException e) {
			throw new BinaryAssertionFailedException(ERROR_MESSAGE,	expected, actual, buildFailReport(e.getMessage()));
		}
	}

	private void checkTables(final IDataSet pExpected, final IDataSet pActual) throws DataSetException {
		
		final int nbTablesExpected = pExpected.getTableNames().length;
		final int nbTablesActual = pActual.getTableNames().length;
		
		if (nbTablesExpected != nbTablesActual){			
			String errorMessage = "The "+VARIABLE_1+" dataset contains "+Math.abs(nbTablesExpected - nbTablesActual)+" more table(s) than the "+VARIABLE_2+" one.";
			if (nbTablesActual > nbTablesExpected){
				errorMessage = buildErrorMessage(errorMessage, FIRST, SECOND);
			}else{
				errorMessage = buildErrorMessage(errorMessage, SECOND, FIRST);
			}			
			throw new DataSetException(errorMessage);
		}	
		
		for (String tableName : pExpected.getTableNames()){
			try{
				pActual.getTable(tableName);
			}catch(NoSuchTableException nste){
				throw new DataSetException("The first dataset does not contains the "+tableName+" dataset.", nste);
			}
		}
	}
	
	private void checkColumnsNumber(final IDataSet pExpected, final IDataSet pActual) throws DataSetException {
		StringBuilder errorMessage = new StringBuilder("");
		for (String tableName : pExpected.getTableNames()){
			ITable tableExpected = new LowerCasedTable(pExpected.getTable(tableName));	
			ITable tableActual = new LowerCasedTable(pActual.getTable(tableName));
			final int nbColumnsExpected = tableExpected.getTableMetaData().getColumns().length;
			final int nbColumnsActual = tableActual.getTableMetaData().getColumns().length;
			
			if (nbColumnsExpected != nbColumnsActual){
				if (!errorMessage.toString().equals("")){
					errorMessage.append("\n");
				}
				
				String message = "Table '"+tableName+"' contains "+Math.abs(nbColumnsExpected - nbColumnsActual)+" more column(s) in the "+VARIABLE_1+" dataset than in the "+VARIABLE_2+" one.";
				if (nbColumnsActual > nbColumnsExpected){
					message = buildErrorMessage(message, FIRST, SECOND);
				}else{
					message = buildErrorMessage(message, SECOND, FIRST);
				}
				errorMessage.append(message);
			}
			if (!errorMessage.toString().equals("")){
				throw new DataSetException(errorMessage.toString());
			}		
		}
	}
	
	private void checkRowsNumber(final IDataSet pExpected, final IDataSet pActual) throws DataSetException {
		StringBuilder errorMessage = new StringBuilder("");
		for (String tableName : pExpected.getTableNames()){
			ITable tableExpected = new LowerCasedTable(pExpected.getTable(tableName));	
			ITable tableActual = new LowerCasedTable(pActual.getTable(tableName));
			int nbRowsExpected = tableExpected.getRowCount();
			int nbRowsActual = tableActual.getRowCount();
			
			if (nbRowsExpected != nbRowsActual){
				if (!errorMessage.toString().equals("")){
					errorMessage.append("\n");
				}

				String message = "Table '"+tableName+"' contains "+Math.abs(nbRowsExpected - nbRowsActual)+" more row(s) in the "+VARIABLE_1+" dataset than in the "+VARIABLE_2+" one.";
				if (nbRowsActual > nbRowsExpected){
					message = buildErrorMessage(message, FIRST, SECOND);
				}else{
					message = buildErrorMessage(message, SECOND, FIRST);
				}
				errorMessage.append(message);
			}
		}
		if (!errorMessage.toString().equals("")){
			throw new DataSetException(errorMessage.toString());
		}
	}
	
	private String buildErrorMessage (String errorMessage, final String variable1, final String variable2){
		String message = errorMessage.replace(VARIABLE_1, variable1);
		message = message.replace(VARIABLE_2, variable2);
		return message;
	}
	
	//this fail report is build in case the two table have a different number of tables, column or rows.
	private List<ResourceAndContext> buildFailReport(String message){
		List<ResourceAndContext> context = new ArrayList<ResourceAndContext>();
		try {
			File tempFile = File.createTempFile("binaryDataSet", ".diff",TempDir.getExecutionTempDir());			
			FileResource diff = new FileResource(tempFile);
			BinaryData diffData = new BinaryData(message.getBytes(
					"UTF-8"));
			diffData.write(tempFile);
			ResourceAndContext diffContext = new ResourceAndContext();
			diffContext.setResource(diff);
			diffContext.setMetadata(new ExecutionReportResourceMetadata(
					getClass(), new Properties(), FileResource.class,
					DIFF_RESOURCE_NAME));
			context.add(diffContext);
		} catch (IOException e) {
			logFailureReportingError(e);
		}
		return context;
	}

	private void compareRows(IDataSet pExpected, IDataSet pActual) throws DatabaseUnitException {
		
		try {
			String[] tableNames = pExpected.getTableNames();

			// Handler which will eventually contain the differences found
			// between Expected dataset and Actual dataset
			DiffCollectingFailureHandler myHandler = new DiffCollectingFailureHandler();
			
			List<String> failedTableNames = new ArrayList<String>();

			// Comparison is made between sorted tables
			for (int i = 0; i < tableNames.length; i++) {
				String tableName = tableNames[i];
				try{
					performTableCompare(tableName, pExpected, pActual, myHandler);
				} catch (TestAssertionFailure taf) {
					failedTableNames.add(tableName);
				}
			}
			
			@SuppressWarnings("unchecked")//check forced by dbunit API...
			List<Difference> diffList = myHandler.getDiffList();
			if (!diffList.isEmpty() || !failedTableNames.isEmpty()) {
				throwAssertionFailure(diffList, failedTableNames);
			}
		} catch (DataSetException bde) {
			throw new BadDataException("Dataset comparison threw dbunit error",
					bde);
		} catch (DatabaseUnitException e) {
			throw new BadDataException("Dataset comparison threw dbunit error",
					e);
		}
	}
	
	private void performTableCompare(String currentTableName, IDataSet pExpected, IDataSet pActual, FailureHandler myHandler) throws DatabaseUnitException{
		ITable expTable = new LowerCasedTable(pExpected.getTable(currentTableName));
		ITable actTable = new LowerCasedTable(pActual.getTable(currentTableName));
		
		ITableMetaData expTableMetaData = expTable.getTableMetaData();
		
		Map<String, Column[]> pkMap = new HashMap<String, Column[]>();
		
		SortedTable expSortedTable = null;
		SortedTable actSortedTable = null;
		
		//We retrieve the primary keys from the expected table (or from the configuration if they are difined by properties)
		Column[] primaryKeys = extractPrimaryKeys(pkMap, expTable);

		//If they don't exist, we retrieve them from the actual table
		if (primaryKeys != null && primaryKeys.length == 0) {
			primaryKeys = extractPrimaryKeys(pkMap, actTable);
		} 
		
		//If they are still not defined, we take the first collumn of the expected table (we have to take something)
		if (primaryKeys != null && primaryKeys.length == 0 && expTableMetaData.getColumns().length > 0){
			primaryKeys = new Column[]{expTableMetaData.getColumns()[0]};
		} 
		//If the expected table has no columns, we it as the empty array it is
		
		if (primaryKeys != null && primaryKeys.length > 0){
			expSortedTable = new SortedTable(expTable, primaryKeys);
			actSortedTable = new SortedTable(actTable, primaryKeys);	
		} else {
			expSortedTable = new SortedTable(expTable);
			actSortedTable = new SortedTable(actTable, expTableMetaData);	
		} 
		
		assertConnector.assertEquals(expSortedTable, actSortedTable,
				myHandler);
	}
	
	private void throwAssertionFailure(List<Difference> diffList, List<String> failedTables) {
		List<ResourceAndContext> context = new ArrayList<ResourceAndContext>();

		try {/*
			 * exceptions during the reporting building process should
			 * not block assertion failure reporting or change the FAIL
			 * state into an ERROR state
			 */
			DiffReportBuilder builder = buildComparisonReport(diffList);

			File tempFile = File.createTempFile("binaryDataSet",".diff",TempDir.getExecutionTempDir());
			
			FileResource diff=new FileResource(tempFile);
			StringBuilder reportBuilder=new StringBuilder(builder.toString());
			if (!failedTables.isEmpty()){
				reportBuilder.append("The Following tables had compare issues (most likely size differences)\n");
				for (String tableName : failedTables) {
					reportBuilder.append(tableName).append("\n");
				}
			}
			BinaryData diffData=new BinaryData(reportBuilder.toString().getBytes("UTF-8"));
			diffData.write(tempFile);
			ResourceAndContext diffContext=new ResourceAndContext();
			diffContext.setResource(diff);
			diffContext.setMetadata(new ExecutionReportResourceMetadata(getClass(), new Properties(), FileResource.class, DIFF_RESOURCE_NAME));
			context.add(diffContext);
		} catch (IOException e) {
			logFailureReportingError(e);
		} catch (DataSetException e) {
			logFailureReportingError(e);
		}

		throw new BinaryAssertionFailedException(
				
				"The actual dataset was different from the expected one",
				expected, actual, context);
	}

	public DiffReportBuilder buildComparisonReport(List<Difference> diffList)
			throws DataSetException {
		
		DiffReportBuilder builder = diffReportBuilderFactory
				.newInstance();
		for (Difference diff : diffList) {
			ITable table = diff.getExpectedTable();

			int rowId = diff.getRowIndex();
			Map<String, String> pkSet = getPkSet(diff);

			builder.addDiffElement(pkSet, rowId, table
					.getTableMetaData().getTableName(), diff
					.getColumnName(), diff.getExpectedValue(), diff
					.getActualValue());
		}
		return builder;
	}
}
