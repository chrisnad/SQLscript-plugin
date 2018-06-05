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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultTableFilter;
import org.squashtest.ta.core.tools.io.BinaryData;
import org.squashtest.ta.framework.annotations.TABinaryAssertion;
import org.squashtest.ta.framework.components.BinaryAssertion;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.framework.exception.BinaryAssertionFailedException;
import org.squashtest.ta.framework.test.result.ResourceAndContext;
import org.squashtest.ta.framework.tools.TempDir;
import org.squashtest.ta.plugin.commons.helpers.ExecutionReportResourceMetadata;
import org.squashtest.ta.plugin.db.library.dbunit.ByTableIncludeExcludeColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.FilteredStructureDataSet;
import org.squashtest.ta.plugin.db.library.dbunit.assertion.DifferenceExtension;
import org.squashtest.ta.plugin.db.library.dbunit.assertion.FailureHandlerExtension;
import org.squashtest.ta.plugin.db.library.dbunit.helper.LowerCasedTable;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;

/**
 * Binary assertion that checks that effective dataset contains expected
 * dataset.
 * 
 * @author edegenetais
 * 
 */

@TABinaryAssertion("contain")
public class DbUnitDatasetContains extends AbstractDbUnitDatasetCompare
		implements
		BinaryAssertion<DbUnitDatasetResource, DbUnitDatasetResource> {

	@Override
	protected void compare(final IDataSet pExpected, final IDataSet pActual) {
		try {
			final FilteredStructureDataSet actualView = buildActualView(
					pExpected, pActual);

			final String[] tableNamesExpected = pExpected.getTableNames();
			final String[] tableNamesActual = actualView.getTableNames();

			// Handler which will eventually contain the differences found
			// between Expected dataset and Actual dataset
			final FailureHandlerExtension myHandler = new FailureHandlerExtension();
			final List<String> notFoundTables = new ArrayList<String>();

			// Comparison is made between sorted tables
			for (String tableExpected : tableNamesExpected) {
				if(isPresent(tableExpected, tableNamesActual, notFoundTables)){
					performTableCompare(tableExpected, pExpected,
						actualView, myHandler);
				}
			}

			if (myHandler.getSize() > 0 || !notFoundTables.isEmpty()) {
				throwAssertionFailure(myHandler.getMap(), notFoundTables,
						"The first dataset did not contain the second one.");
			}
		} catch (DataSetException bde) {
			throw new BadDataException("Dataset comparison threw dbunit error",
					bde);
		} catch (DatabaseUnitException e) {
			throw new BadDataException("Dataset comparison threw dbunit error",
					e);
		}
	}
	

	/**
	 * 
	 * 
	 * @param tableExpected
	 * @param tableNamesActual
	 * @param notFoundTables
	 * @return return true if the table is found
	 */
	private boolean isPresent(final String tableExpected,
			final String[] tableNamesActual, final List<String> notFoundTables) {
		boolean found = false;
		for (String tableActual : tableNamesActual) {
			if (tableExpected.equals(tableActual)) {
				found = true;
				break;
			}
		}
		if (!found) {
			notFoundTables
					.add("Table '"
							+ tableExpected
							+ "' is present in the second dataset but not in the first one.");
		}
		return found;
	}

	private FilteredStructureDataSet buildActualView(IDataSet expectedDataset,
			IDataSet actualDataset) {
		try {
			DefaultTableFilter containsTableFilter = new DefaultTableFilter();
			ByTableIncludeExcludeColumnFilter containsColumnFilter = new ByTableIncludeExcludeColumnFilter();
			String[] tableNames = expectedDataset.getTableNames();
			for (String name : tableNames) {
				containsTableFilter.includeTable(name);
				ITableMetaData metaData = expectedDataset
						.getTableMetaData(name);
				for (Column column : metaData.getColumns()) {
					containsColumnFilter.addColumnIncludeFilter(name,
							column.getColumnName());
				}
			}
			return new FilteredStructureDataSet(actualDataset,
					containsTableFilter, containsColumnFilter);

		} catch (DataSetException dse) {
			throw new BadDataException("Dataset assert error.", dse);
		}
	}

	private void performTableCompare(String currentTableName,
			IDataSet pExpected, IDataSet pActual, FailureHandler myHandler)
			throws DatabaseUnitException {
		ITable expTable = new LowerCasedTable(
				pExpected.getTable(currentTableName));
		ITable actTable = new LowerCasedTable(
				pActual.getTable(currentTableName));

		Column[] primaryKeys = extractPrimaryKeys(
				pExpected.getTableMetaData(currentTableName),
				pActual.getTableMetaData(currentTableName));
		List<String> primaryKeysName = getColumnName(primaryKeys);
		if (primaryKeys == NO_PK_VALUE) {
			assertConnector.assertContains(expTable, actTable, myHandler,
					primaryKeysName);
		} else {
			SortedTable expSortedTable = new SortedTable(expTable, primaryKeys);
			SortedTable actSortedTable = new SortedTable(actTable, primaryKeys);
			assertConnector.assertContains(expSortedTable, actSortedTable,
					myHandler, primaryKeysName);
		}
	}

	protected void throwAssertionFailure(
			Map<String, List<DifferenceExtension>> map,
			List<String> notFoundTables, String message) {
		throw new BinaryAssertionFailedException(message, expected, actual,
				buildDiffReport(map, notFoundTables));
	}

	private List<ResourceAndContext> buildDiffReport(
			Map<String, List<DifferenceExtension>> map,
			List<String> notFoundTables) {
		List<ResourceAndContext> context = new ArrayList<ResourceAndContext>();

		try {

			// The use of the DiffReportBuilder is temporary bypass.
			// Restore it's use would be a good idea
			// DiffReportBuilder builder =
			// diffReportBuilderFactory.newInstance();

			StringBuilder builder = new StringBuilder();
			builder.append("The dataset did not contain the expected data.").append("\n").append("\n");
			if(!notFoundTables.isEmpty()){
				builder.append("* Some table(s) was(were) not found:\n");
				for (String message : notFoundTables) {
					builder.append("\t- ").append(message).append("\n");
				}
				builder.append("\n").append("\n");
			}
			
			if(!map.isEmpty())
			{
				for (String tableName : map.keySet()) {
					buildDiffTableReport(map, tableName, builder);
				}
			}

			File tempFile = File.createTempFile("binaryDataSet", ".diff", TempDir.getExecutionTempDir());
			FileResource diff = new FileResource(tempFile);
			BinaryData diffData = new BinaryData(builder.toString().getBytes(
					"UTF-8"));
			diffData.write(tempFile);
			ResourceAndContext diffContext = new ResourceAndContext();
			diffContext.setResource (diff);
			diffContext.setMetadata ( new ExecutionReportResourceMetadata(
					getClass(), new Properties(), FileResource.class,
					DIFF_RESOURCE_NAME));
			context.add(diffContext);
		} catch (IOException e) {
			logFailureReportingError(e);
		} catch (DataSetException e) {
			logFailureReportingError(e);
		}
		return context;
	}

	private void buildDiffTableReport(
			Map<String, List<DifferenceExtension>> map, String tableName,
			StringBuilder builder) throws DataSetException {
		List<DifferenceExtension> diffList = map.get(tableName);
		ITable table = diffList.get(0).getExpectedTable();
		ITableMetaData metadata = table.getTableMetaData();
		Column[] colList = metadata.getColumns();

		// A table which did not contains primary key has a personalized error
		// message
		DifferenceExtension.PotentialMatchStatus pms = diffList.get(0)
				.getPotentialMatchstatus();
		boolean disabled = pms
				.equals(DifferenceExtension.PotentialMatchStatus.DISABLED);

		builder.append("* Table \"");
		builder.append(tableName);
		builder.append("\" did not contains the following row(s):\n");

		String tab;
		String primaryKeys = "";
		for (DifferenceExtension diff : diffList) {
			primaryKeys = diff.getPrimaryKeys().toString();
			tab = "\t";
			rowBuilder(builder, tab, table, colList, diff.getRowIndex());
			tab = "\t\t";
			DifferenceExtension.PotentialMatchStatus status = diff
					.getPotentialMatchstatus();
			switch (status) {
			case NO_MATCH:
				builder.append("\t\t --> No match found in the actual dataset by reducing the search to primary key column(s).\n");
				builder.append("\n");
				break;

			case ONE_MATCH:
				builder.append("\t\t --> The following match was found by reducing the search to the primary key column(s):\n");
				rowBuilder(builder, tab, diff.getActualTable(), colList,
						diff.getPossibleRowIndex());
				builder.append("\n");

				break;

			case MANY_MATCH:
				builder.append("\t\t --> Several matches were found by reducing the search to the primary key column(s). The first match is:\n");
				rowBuilder(builder, tab, diff.getActualTable(), colList,
						diff.getPossibleRowIndex());
				builder.append("\n");
				break;

			default:
				// Disabled status
				break;
			}

		}
		// A table which did not contains primary key has a personalized error
		// message
		if (disabled) {
			builder.append("Note : As table \"");
			builder.append(tableName);
			builder.append("\" has no (pseudo) primary key, it is not possible to match the rows from the two datasets.");
		} else {
			builder.append("Note : The (pseudo) primary key used for table  \"");
			builder.append(tableName);
			builder.append("\" is: ");
			builder.append(primaryKeys);
			builder.append(".");

		}
		builder.append("\n");
		builder.append("\n");
		builder.append("\n");

	}

	private void rowBuilder(StringBuilder builder, String tab, ITable table,
			Column[] colList, int rowIndex) throws DataSetException {
		builder.append(tab);
		builder.append(" -");
		for (Column column : colList) {
			String columnName = column.getColumnName();
			builder.append(" {");
			builder.append(columnName);
			builder.append("='");
			builder.append(table.getValue(rowIndex, columnName));
			builder.append("'}");
		}
		builder.append("\n");
	}
}
