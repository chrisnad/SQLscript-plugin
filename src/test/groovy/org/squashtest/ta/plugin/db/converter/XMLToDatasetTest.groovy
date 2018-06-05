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

import static org.junit.Assert.*

import org.dbunit.dataset.Column
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.ITableIterator
import org.squashtest.ta.framework.exception.BadDataException
import org.squashtest.ta.plugin.commons.resources.XMLResource
import org.squashtest.ta.plugin.db.converter.XmlToDataset;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;

import spock.lang.Specification

class XMLToDatasetTest extends Specification {

	def "the XML file must be correctly formated"(){
	
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/validXMLFile.xml")
			File file = new File(url.toURI())
			XMLResource myXMLResource = Mock()
			myXMLResource.getXMLFile() >> file
			XmlToDataset datasetConverter = new XmlToDataset()
		when :
			DbUnitDatasetResource datasetResource = datasetConverter.convert(myXMLResource)
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
					columnValuesInTablesColumns.put(myTable.getTableMetaData().getTableName() + "-" + myColumns[i].getColumnName(), 
						columnValues)
				}
				columnNamesInTables.put(myTable.getTableMetaData().getTableName(), columnNames)
				
			}
		then :
			datasetResource != null
			tableNames == ["TEST_TABLE", "SECOND_TABLE", "EMPTY_TABLE"]
			columnNamesInTables.get("TEST_TABLE") == ["COL0", "COL1", "COL2"]
			columnNamesInTables.get("SECOND_TABLE") == ["COL0", "COL1"]
			columnNamesInTables.get("EMPTY_TABLE") == []
			columnValuesInTablesColumns.get("TEST_TABLE-COL0") == ["TT row 0 col 0", "TT row 1 col 0", null]
			columnValuesInTablesColumns.get("TEST_TABLE-COL1") == ["TT row 0 col 1", "TT row 1 col 1", "TT row 2 col 1"]
			columnValuesInTablesColumns.get("SECOND_TABLE-COL1") == ["ST row 0 col 1", null]
			notThrown(BadDataException) 
	}
}
