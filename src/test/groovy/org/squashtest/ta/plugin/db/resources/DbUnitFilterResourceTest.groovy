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
/**
*     This file is part of the Squashtest platform.
*     Copyright (C) 2011 - 2011 Squashtest TA, Squashtest.org
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
package org.squashtest.ta.plugin.db.resources

import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.squashtest.ta.plugin.db.resources.DbUnitFilterResource;

import spock.lang.Specification

class DbUnitFilterResourceTest extends Specification {
	def testee
	def	tableFilter
	def columnFilter
	def table1
	def table2
	def table3
	def column11
	def column12
	def column13
	def column21
	def column22
	def column23
	def column31
	def column32
	def column33
	def dataset
	
	def setup(){
		tableFilter=Mock(ITableFilter)
		tableFilter.accept("Table1")>>true
		tableFilter.accept("Table2")>>false
		tableFilter.accept("Table3")>>true
		tableFilter.getTableNames(testee)>>["Table1","Table3"]
		
		column11=Mock(Column)
		column12=Mock(Column)
		column13=Mock(Column)
		
		table1=Mock(ITable)
		def metadata1=Mock(ITableMetaData)
		metadata1.getTableName()>>"Table1"
		metadata1.getColumns()>>[column11,column12,column13].toArray(new Column[0])
		table1.getTableMetaData()>>metadata1
		
		column21=Mock(Column)
		column22=Mock(Column)
		column23=Mock(Column)
		
		table2=Mock(ITable)
		def metadata2=Mock(ITableMetaData)
		metadata2.getTableName()>>"Table2"
		metadata2.getColumns()>>[column21,column22,column23].toArray(new Column[0])
		table2.getTableMetaData()>>metadata2
		
		column31=Mock(Column)
		column32=Mock(Column)
		column33=Mock(Column)
		
		table3=Mock(ITable)
		def metadata3=Mock(ITableMetaData)
		metadata3.getTableName()>>"Table3"
		metadata3.getColumns()>>[column31,column32,column33].toArray(new Column[0])
		table3.getTableMetaData()>>metadata3
		
		columnFilter=Mock(IColumnFilter)
		columnFilter.accept("Table1",column12)>>true
		columnFilter.accept("Table2",column23)>>true
		columnFilter.accept("Table3",column31)>>true
		
		def dsIterator=Mock(ITableIterator)
		dsIterator.next()>>>[true,true,true,false]
		dsIterator.getTable()>>>[table1,table2,table3]
		
		def dsRevIterator=Mock(ITableIterator)
		dsRevIterator.next()>>>[true,true,true,false]
		dsRevIterator.getTable()>>>[table3,table2,table1]
		
		dataset=Mock(IDataSet)
		dataset.getTableNames()>>["Table1","Table2","Table3"]
		dataset.getTables()>>[table1,table2,table3]
		dataset.getTable("Table1")>>table1
		dataset.getTable("Table2")>>table2
		dataset.getTable("Table3")>>table3
		dataset.getTableMetaData("Table1")>>table1.getTableMetaData()
		dataset.getTableMetaData("Table2")>>table2.getTableMetaData()
		dataset.getTableMetaData("Table3")>>table3.getTableMetaData()
		dataset.iterator()>>dsIterator
		dataset.reverseIterator()>>dsRevIterator
		
		def tableIterator=Mock(ITableIterator)
		tableIterator.next()>>>[true,false]
		tableIterator.getTable()>>>[table2]
		tableFilter.iterator(dataset,true)>>tableIterator
		tableFilter.iterator(dataset,false)>>tableIterator
	}
	
	def "should apply its table filter to datasets"(){
		given:
			testee=new DbUnitFilterResource(tableFilter, columnFilter,false,null)
		when:
			def decoratedDs=testee.apply(dataset)
			def tableNames=decoratedDs.getTableNames()
		then:
			tableNames==["Table2"]
	}
	
	def "should apply its column filter to datasets"(){
		given:
			testee=new DbUnitFilterResource(tableFilter, columnFilter,false,null)
		when:
			def decoratedDs=testee.apply(dataset)
			def tableTwo=decoratedDs.getTable("Table2")
			def columns=tableTwo.getTableMetaData().getColumns()
		then:
			columns==[column23]
	}
	
	def "should expose all tables when built without table filter"(){
		given:
		testee=new DbUnitFilterResource(null, columnFilter,false,null)
	when:
		def decoratedDs=testee.apply(dataset)
		def tableNames=new HashSet(Arrays.asList(decoratedDs.getTableNames()))
	then:
		tableNames==new HashSet(["Table1","Table2","Table3"])
	}
	
	def "should still filter columns when built without table filter"(){
		given:
			testee=new DbUnitFilterResource(null, columnFilter,false,null)
		when:
			def decoratedDs=testee.apply(dataset)
			def tableTwo=decoratedDs.getTable("Table2")
			def columns=tableTwo.getTableMetaData().getColumns()
		then:
			columns==[column23]
	}
	
	def "should still filter tables when built without columns filter"(){
		given:
		testee=new DbUnitFilterResource(tableFilter, null,false,null)
	when:
		def decoratedDs=testee.apply(dataset)
		def tableNames=decoratedDs.getTableNames()
	then:
		tableNames==["Table2"]
	}
	
	def "should expose all columns when built without column filter"(){
		given:
			testee=new DbUnitFilterResource(tableFilter, null,false,null)
		when:
			def decoratedDs=testee.apply(dataset)
			def tableTwo=decoratedDs.getTable("Table2")
			def columns=new HashSet(Arrays.asList(tableTwo.getTableMetaData().getColumns()))
		then:
			columns==new HashSet([column21,column22,column23])
	}
}
