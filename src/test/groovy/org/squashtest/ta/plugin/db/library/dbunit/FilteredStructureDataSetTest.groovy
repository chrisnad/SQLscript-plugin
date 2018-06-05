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
package org.squashtest.ta.plugin.db.library.dbunit

import org.dbunit.dataset.Column
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.ITableIterator
import org.dbunit.dataset.ITableMetaData
import org.dbunit.dataset.filter.IColumnFilter
import org.dbunit.dataset.filter.ITableFilter
import org.squashtest.ta.plugin.db.library.dbunit.FilteredStructureDataSet;
import org.squashtest.ta.plugin.db.library.dbunit.helpers.YesTableFilter;

import spock.lang.Specification

class FilteredStructureDataSetTest extends Specification{
	def testee
	def column1
	def column2
	def column3
	def table1
	def table2
	def table3
	
	def setup(){
		table1=Mock(ITable)
		def metadata1=Mock(ITableMetaData)
		def columna=Mock(Column)
		table1.getTableMetaData()>>metadata1
		metadata1.getTableName()>>"Table1"
		metadata1.getColumns()>>[columna].toArray(new Column[0])
		
		table2=Mock(ITable)
		def metadata2=Mock(ITableMetaData)
		column1=Mock(Column)
		column2=Mock(Column)
		column3=Mock(Column)
		table2.getTableMetaData()>>metadata2
		metadata2.getTableName()>>"Table2"
		metadata2.getColumns()>>[column1,column2,column3].toArray(new Column[0])
		
		table3=Mock(ITable)
		def metadata3=Mock(ITableMetaData)
		def columnb=Mock(Column)
		table3.getTableMetaData()>>metadata3
		metadata3.getTableName()>>"Table3"
		metadata3.getColumns()>>[columnb].toArray(new Column[0])
	}
	
	def "should only show unfiltered tables"(){
		given:
			def dataset=Mock(IDataSet)
			dataset.getTableNames()>>["Table1","Table2","Table3"]
		and:
			def tableFilter=Mock(ITableFilter)
			tableFilter.accept(_)>>false
			tableFilter.accept("Table2")>>true
			tableFilter.getTableNames(testee)>>["Table2"]
			
			def iterator=Mock(ITableIterator)
			iterator.next()>>>[true,false]
			iterator.getTable()>>>[table2]
			tableFilter.iterator(_,_)>>iterator
		and:
			testee=new FilteredStructureDataSet(dataset, tableFilter, null);
		when:
			def tableNames=testee.getTableNames()
		then:
			tableNames==["Table2"]
	}
	
	def "should only show unfiltered columns in a table"(){
		given:
			def columnFilter=Mock(IColumnFilter)
			columnFilter.accept("Table2",column2)>>true
		and:
			def iterator=Mock(ITableIterator)
			iterator.next()>>>[true,true,true,false]
			iterator.getTable()>>>[table1,table2,table3]
			def reverseItr=Mock(ITableIterator)
			reverseItr.next()>>>[true,true,true,false]
			reverseItr.getTable()>>>[table3,table2,table1]
		and:
			def dataset=Mock(IDataSet)
			dataset.getTableNames()>>["Table1","Table2","Table3"]
			dataset.getTables()>>[table1,table2,table3]
			dataset.iterator()>>iterator
			dataset.reverseIterator()>>reverseItr
		and:
			testee=new FilteredStructureDataSet(dataset, new YesTableFilter(), columnFilter);
	when:
		def table=testee.getTable("Table2")
		def tableMetaData=table.getTableMetaData()
	then:
		tableMetaData.getColumns()==[column2]
	}
}
