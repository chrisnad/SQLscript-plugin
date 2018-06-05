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
package org.squashtest.ta.plugin.db.library.dbunit.assertion;

import java.util.ArrayList;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.Columns;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.exception.BrokenTestException;

/**
 * This class extends dbunit DbUnitAssert classes. 
 * It was created in order to add the support of the assert contains feature.   
 *
 */
public class DbUnitAssertExtension extends DbUnitAssert {

	/**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbUnitAssert.class);
	
    private List<String> primaryKeysName;
    
    /**
     * This check if the expected dataset is contains inside the actual dataset.
     * The actual dataset could have more column and / or more row than the expected dataset.</br>
     * If a not null or not empty list of primary key is given in argument, then the method try to 
     * give a row from the actual dataset that could match a not found row from the expected dataset.
     * For this potential match, only primary column rows are used for matching. 
     *   
     * 
     * @param expectedTable The expected dataset
     * @param actualTable The actual dataset
     * @param failureHandler The assert exception handler
     * @param primaryKeysName The list of primary key to use for potential match feature. 
     * @throws DatabaseUnitException Exception occurs the assert contains process. 
     */
    public void assertContains(ITable expectedTable, ITable actualTable,
            FailureHandler failureHandler, List<String> primaryKeysName) throws DatabaseUnitException
    {
    	
    	LOGGER.debug(
                "assertEquals(expectedTable={}, actualTable={}, failureHandler={}) - start",
                new Object[] { expectedTable, actualTable, failureHandler });
    	
    	this.primaryKeysName = primaryKeysName;
    	
        // Do not continue if same instance
        if (expectedTable.equals(actualTable)) {
            LOGGER.debug(
                            "The given tables reference the same object. Will return immediately. (Table={})",
                            expectedTable);
            return;
        }

        ITableMetaData expectedMetaData = expectedTable.getTableMetaData();
        ITableMetaData actualMetaData = actualTable.getTableMetaData();
        String expectedTableName = expectedMetaData.getTableName();

        // Verify row count
        int expectedRowsCount = expectedTable.getRowCount();
        int actualRowsCount = actualTable.getRowCount();
        if (expectedRowsCount > actualRowsCount) {
        	StringBuilder builder = new StringBuilder(" Table '");
        	builder.append(expectedTableName);
        	builder.append("' has more rows in the second dataset than in the first one. ");
        	builder.append("The second dataset can't be included in the first one.");
            Error error = failureHandler.createFailure(builder.toString(), 
            		String.valueOf(expectedRowsCount), String.valueOf(actualRowsCount));
            LOGGER.error(error.toString());
            throw error;
        }

        if (expectedRowsCount == 0 && actualRowsCount == 0) {
            LOGGER.debug("Tables are empty, hence equals.");
            return;
        }

        // Put the columns into the same order
        Column[] expectedColumns = Columns.getSortedColumns(expectedMetaData);
        Column[] actualColumns = Columns.getSortedColumns(actualMetaData);

        // Verify columns
        Columns.ColumnDiff columnDiff =
                Columns.getColumnDiff(expectedMetaData, actualMetaData);
        if (columnDiff.getExpected().length > 0) {
        	String message = columnDiff.getMessage();
            throw failureHandler.createFailure(message, Columns
                    .getColumnNamesAsString(expectedColumns), Columns
                    .getColumnNamesAsString(actualColumns));
        }

        // Get the datatypes to be used for comparing the sorted columns
        ComparisonColumn[] comparisonCols = getComparisonColumns(expectedTableName,
                expectedColumns, actualColumns, failureHandler);

        // Finally compare the data
        compareData(expectedTable, actualTable, comparisonCols, failureHandler);
    }
    
    @Override
    protected ComparisonColumn[] getComparisonColumns(String expectedTableName,
            Column[] expectedColumns, Column[] actualColumns,
            FailureHandler failureHandler) 
    {
        ComparisonColumn[] result = new ComparisonColumn[expectedColumns.length];
        int actualColumnIndex = 0;
        for (int expectedColumnIndex = 0; expectedColumnIndex < expectedColumns.length; expectedColumnIndex++) {
            Column expectedColumn = expectedColumns[expectedColumnIndex];
            Column actualColumn = actualColumns[actualColumnIndex];
            while (!expectedColumn.getColumnName().equals(actualColumn.getColumnName())&& actualColumnIndex < actualColumns.length)
            {
            	actualColumnIndex++;
            	actualColumn = actualColumns[actualColumnIndex];
            }
            result[expectedColumnIndex] = new ComparisonColumn(expectedTableName, expectedColumn,
                    actualColumn, failureHandler);
        }
        return result;
    }
    
    
    @Override
    protected void compareData(ITable expectedTable, ITable actualTable,
            ComparisonColumn[] comparisonCols, FailureHandler failureHandler)
            throws DataSetException
    {
        LOGGER.debug("compareData(expectedTable={}, actualTable={}, "
                + "comparisonCols={}, failureHandler={}) - start",
                new Object[] {expectedTable, actualTable, comparisonCols,
                        failureHandler});

        if (expectedTable == null) {
            throw new BrokenTestException(
                    "The parameter 'expectedTable' must not be null");
        }
        if (actualTable == null) {
            throw new BrokenTestException(
                    "The parameter 'actualTable' must not be null");
        }
        if (comparisonCols == null) {
            throw new BrokenTestException(
                    "The parameter 'comparisonCols' must not be null");
        }
        if (failureHandler == null) {
            throw new BrokenTestException(
                    "The parameter 'failureHandler' must not be null");
        }

        List<Integer> possibleRowFromActual = new ArrayList<Integer>(actualTable.getRowCount());
    	for (int index = 0; index < actualTable.getRowCount(); index++) {
    			possibleRowFromActual.add(index);
		}
    	
    	List<DifferenceExtension> diffList = new ArrayList<DifferenceExtension>();
    	
        // iterate over all rows
        for (int rowFromExpected = 0; rowFromExpected < expectedTable.getRowCount(); rowFromExpected++) {
        	
        	List<Integer> possibleRowForExpectedLine = new ArrayList<Integer>(possibleRowFromActual);
        	
            for (int column = 0; column < comparisonCols.length; column++) {
                ComparisonColumn compareColumn = comparisonCols[column];
                String columnName = compareColumn.getColumnName();
                DataType dataType = compareColumn.getDataType();
                List<Integer> toRemove = new ArrayList<Integer>();
                for (Integer lineFromActual : possibleRowForExpectedLine) {
                	Object expectedValue = expectedTable.getValue(rowFromExpected, columnName);
                    Object actualValue = actualTable.getValue(lineFromActual, columnName);
                    if (dataType.compare(expectedValue, actualValue) != 0) {
                    	toRemove.add(lineFromActual);
                    }
                }
                possibleRowForExpectedLine.removeAll(toRemove);
            }
            if (possibleRowForExpectedLine.size()==1){
            	// Row lineFromExpected from expected table match the row from actual table contains in the List
            	possibleRowFromActual.remove(possibleRowForExpectedLine.get(0));
            } else if(possibleRowForExpectedLine.size() > 1) {
            	// The row lineFromExpected from expected table match the rows from actual table contains in the List
            	// => Many rows match we take the first row for match.

            	// We remove the selected row from the list of possible row
            	possibleRowFromActual.remove(possibleRowForExpectedLine.get(0));
            } else {
            	// No match found
            	DifferenceExtension diff = new DifferenceExtension(expectedTable, actualTable, rowFromExpected);
            	diffList.add(diff);
            	failureHandler.handle(diff);
            }	
            
        }
        
  
        if(primaryKeysName.size() > 0 ){
            // The potential match by reducing the column to primary key column
        	
        	for (DifferenceExtension difference : diffList) {
        		int rowIndex = difference.getRowIndex();
        		List<Integer> possibleRowForExpectedLine = new ArrayList<Integer>(possibleRowFromActual);
                for (int column = 0; column < comparisonCols.length; column++) {
                	
                    ComparisonColumn compareColumn = comparisonCols[column];
                    String columnName = compareColumn.getColumnName();
                    
                    if(primaryKeysName.contains(columnName)){
	                
                    	DataType dataType = compareColumn.getDataType();
	                    List<Integer> toRemove = new ArrayList<Integer>();
	                    for (Integer lineFromActual : possibleRowForExpectedLine) {
	                    	Object expectedValue = expectedTable.getValue( rowIndex, columnName);
	                        Object actualValue = actualTable.getValue(lineFromActual, columnName);
	                        if (dataType.compare(expectedValue, actualValue) != 0) {
	                        	toRemove.add(lineFromActual);
	                        }
	                    }
	                    possibleRowForExpectedLine.removeAll(toRemove);
	                    
                    }
                }
                if (possibleRowForExpectedLine.size()==1){
                	// Row lineFromExpected from expected table match the row from actual table contains in the List
                	difference.addOnePotentialMatch(possibleRowForExpectedLine.get(0),primaryKeysName);
                } else if(possibleRowForExpectedLine.size() > 1) {
                	// The row lineFromExpected from expected table match the rows from actual table contains in the List
                	// => Many rows match we take the first row for match.

                	// We remove the selected row from the list of possible row
                	difference.addManyPotentialMatch(possibleRowForExpectedLine.get(0),primaryKeysName);
                } else {
                	// No match found 
                	difference.addNoPotentialMatch(primaryKeysName);
                }
    		}
        }
        

    }
	
}
