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
package org.squashtest.ta.plugin.db.converter;

import java.io.File
import java.lang.reflect.Array;
import java.util.Properties

import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.squashtest.ta.plugin.commons.resources.DirectoryResource
import org.squashtest.ta.framework.exception.BadDataException
import org.squashtest.ta.plugin.db.converter.CSVToDataset;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;

import static org.junit.Assert.*

import spock.lang.Specification

class CSVToDatasetTest extends Specification {

	def "the CSV files must be correctly formated"(){
	
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/CSVDir")
			File file = new File(url.toURI())
			DirectoryResource myDirectoryResource = Mock()
			myDirectoryResource.getDirectory() >> file
			CSVToDataset datasetConverter = new CSVToDataset()
		when :
			DbUnitDatasetResource datasetResource = datasetConverter.convert(myDirectoryResource)
			IDataSet myDataSet = datasetResource.getDataset()
			List<String> tableNames = new ArrayList<String>()
			ITableIterator myIterator = myDataSet.iterator()
			Map<String, List<String>> columnNamesInTables = new HashMap<String, List<String>>()
			Map<String, List<String>> columnValuesInTablesColumns = new HashMap<String, List<String>>()
			while(myIterator.next()) {
				ITable myTable = myIterator.getTable() 
				int nbTableRows = myTable.getRowCount()
				tableNames.add(myTable.getTableMetaData().getTableName())
				List<String> columnNames = new ArrayList<String>()
				Column[] myColumns = myTable.getTableMetaData().getColumns()
				for (int i=0; i<myColumns.length; i++){
					columnNames.add(myColumns[i].getColumnName())
					List<String> columnValues = new ArrayList<String>()
					for (int j = 0; j < nbTableRows; j++) {
						columnValues.add(myTable.getValue(j, myColumns[i].getColumnName()))
					}
					columnValuesInTablesColumns.put(myTable.getTableMetaData().getTableName() + "-" + myColumns[i].getColumnName(),columnValues)
				}
				columnNamesInTables.put(myTable.getTableMetaData().getTableName(), columnNames)
			}
		then :
			datasetResource != null
			tableNames == ["table01", "table02", "table03"]
			columnNamesInTables.get("table01") == ["TAB1_COL1", "TAB1_COL2", "TAB1_COL3", "TAB1_COL4"]
			columnNamesInTables.get("table02") == ["TAB2_COL1", "TAB2_COL2", "TAB2_COL3", "TAB2_COL4", "TAB2_COL5"]
			columnNamesInTables.get("table03") == ["TAB3_COL1", "TAB3_COL2"]
			columnValuesInTablesColumns.get("table01-TAB1_COL1") == ["TAB1_COL1_val1", "TAB1_COL1_val2", "TAB1_COL1_val3"]
			columnValuesInTablesColumns.get("table01-TAB1_COL2") == ["TAB1_COL2_val1", null, "TAB1_COL2_val3"]
			columnValuesInTablesColumns.get("table03-TAB3_COL1") == ["TAB3_COL1_val1", "TAB3_COL1_val2", "TAB3_COL1_val3", "TAB3_COL1_val4", "TAB3_COL1_val5", "TAB3_COL1_val6", null, "TAB3_COL1_val8", "TAB3_COL1_val9"]
			
			notThrown(BadDataException) 
	}
}
