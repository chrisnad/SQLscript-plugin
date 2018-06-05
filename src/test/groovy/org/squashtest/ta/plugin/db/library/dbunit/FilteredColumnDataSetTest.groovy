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

import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.filter.IColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.FilteredColumnDataSet;

import spock.lang.Specification;

class FilteredColumnDataSetTest extends Specification{
	def testee
	def dataset
	def column1
	def column2
	def column3
	
	def setup(){
		dataset=Mock(IDataSet)
		dataset.getTableNames()>>["Table"]
		def table=Mock(ITable)
		def metadata=Mock(ITableMetaData)
		column1=Mock(Column)
		column2=Mock(Column)
		column3=Mock(Column)
		table.getTableMetaData()>>metadata
		metadata.getTableName()>>"Table"
		metadata.getColumns()>>[column1,column2,column3].toArray(new Column[0])
		dataset.getTable("Table")>>table
		def iterator=Mock(ITableIterator)
		dataset.iterator()>>iterator
		iterator.next()>>>[true,false]
		iterator.getTable()>>>[table]
	}
	
	def "should expose only unfiltered columns"(){
		given:
			def filter=Mock(IColumnFilter)
			filter.accept(_,column1)>>false
			filter.accept(_,column2)>>true
			filter.accept(_,column3)>>false
		and:
			testee=new FilteredColumnDataSet(dataset, filter)
		when:
			def filteredTable=testee.getTable("Table")
			def filteredMetadata=filteredTable.getTableMetaData();
		then:
			filteredMetadata.getColumns()==[column2]
	}
}
