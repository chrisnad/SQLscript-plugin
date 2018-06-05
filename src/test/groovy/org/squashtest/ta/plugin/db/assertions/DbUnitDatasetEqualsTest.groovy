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
package org.squashtest.ta.plugin.db.assertions

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.squashtest.ta.plugin.db.assertions.DbUnitDatasetEquals;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;
import org.squashtest.ta.plugin.db.resources.DbUnitFilterResource;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.exception.AssertionFailedException;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.framework.exception.BinaryAssertionFailedException;

import spock.lang.Specification

class DbUnitDatasetEqualsTest extends Specification {
	def testee
	def expected
	def actual
	def table1
	def table2
	def column21
	def column22
	def metadata1
	def metadata2
	def iteratorActual
	def revIteratorActual
	
	def setup(){
		testee=new DbUnitDatasetEquals()
		
		expected=Mock(IDataSet)
		actual=Mock(IDataSet)
		
		table1=Mock(ITable)
		metadata1=Mock(ITableMetaData)
		table1.getTableMetaData()>>metadata1
		metadata1.getTableName()>>"table1"
		def column11=Mock(Column)
		column11.getColumnName()>>"column11"
		column11.getDataType() >> DataType.VARCHAR
		def column12=Mock(Column)
		column12.getColumnName()>>"column12"
		column12.getDataType() >> DataType.VARCHAR
		metadata1.getColumns()>>[column11,column12]
		table1.getRowCount()>>3
		table1.getValue(0,"column11")>>"0_11"
		table1.getValue(0,"column12")>>"0_12"
		table1.getValue(1,"column11")>>"1_11"
		table1.getValue(1,"column12")>>"1_12"
		table1.getValue(2,"column11")>>"2_11"
		table1.getValue(2,"column12")>>"2_12"

				
		table2=Mock(ITable)
		metadata2=Mock(ITableMetaData)
		table2.getTableMetaData()>>metadata2
		metadata2.getTableName()>>"table2"
		column21=Mock(Column)
		column21.getColumnName()>>"column21"
		column21.getDataType() >> DataType.VARCHAR
		column22=Mock(Column)
		column22.getColumnName()>>"column22"
		column22.getDataType() >> DataType.VARCHAR
		metadata2.getColumns()>>[column21,column22]
		table2.getRowCount()>>2
		table2.getValue(0,"column21")>>"0_21"
		table2.getValue(0,"column22")>>"0_22"
		table2.getValue(1,"column21")>>"1_21"
		table2.getValue(1,"column22")>>"1_22"
		
		expected=new DefaultDataSet([table1,table2].toArray(new ITable[0]))
	}
	
	def "if less tables in actual, dbunit exception"(){
		given:
			actual=new DefaultDataSet([table2].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			testee.test()
		then:
			BinaryAssertionFailedException failure=thrown()
			//expected: report
			failure.getFailureContext()!=null
			failure.getFailureContext().size()==1
			failure.getFailureContext().get(0).resource instanceof FileResource
	}
	
	def "if more tables in actual, dbunit exception"(){
		given:
			actual=new DefaultDataSet([table1,table2].toArray(new ITable[0]))
			expected=new DefaultDataSet([table2].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			testee.test()
		then:
			BinaryAssertionFailedException failure=thrown()
			//expected: report
			failure.getFailureContext()!=null
			failure.getFailureContext().size()==1
			failure.getFailureContext().get(0).resource instanceof FileResource
	}
	
	def "if different tables in actual, dbunit exception"(){
		given:
			actual=new DefaultDataSet([table1].toArray(new ITable[0]))
			expected=new DefaultDataSet([table2].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			testee.test()
		then:
			BinaryAssertionFailedException failure=thrown()
			//expected: report
			failure.getFailureContext()!=null
			failure.getFailureContext().size()==1
			failure.getFailureContext().get(0).resource instanceof FileResource
	}
	
	def "only unfiltered tables are looked for"(){
		given:
			actual=new DefaultDataSet([table2].toArray(new ITable[0]))
		and:
			def tableFilter=Mock(ITableFilter)
			tableFilter.getTableNames(_)>>["table2"]
			def ITableIterator filterIterator1=Mock(ITableIterator)
			filterIterator1.next()>>>[true,false]
			filterIterator1.getTable()>>>[table2]
			filterIterator1.getTableMetaData()>>>[metadata2]
			tableFilter.iterator(actual,_)>>filterIterator1
			def ITableIterator filterIterator2=Mock(ITableIterator)
			filterIterator2.next()>>>[true,false]
			filterIterator2.getTable()>>>[table2]
			filterIterator2.getTableMetaData()>>>[metadata2]
			tableFilter.iterator(expected,_)>>filterIterator2
		and:
			testee.addConfiguration([new DbUnitFilterResource(tableFilter)])
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			def ok=false
			testee.test()
			ok=true
		then:
			ok
	}
	
	def "if less columns, dbunit exception"(){
		given:
			def table2_a=Mock(ITable)
			def metadata2_a=Mock(ITableMetaData)
			table2_a.getTableMetaData()>>metadata2_a
			metadata2_a.getTableName()>>"table2"
			metadata2_a.getColumns()>>[column22]
			table2_a.getRowCount()>>2
			table2_a.getValue(0,"column22")>>"0_22"
			table2_a.getValue(1,"column22")>>"1_22"
		and:
			actual=new DefaultDataSet([table1, table2_a].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			testee.test()
		then:
			BinaryAssertionFailedException failure=thrown()
			//expected: report
			failure.getFailureContext()!=null
			failure.getFailureContext().size()==1
			failure.getFailureContext().get(0).resource instanceof FileResource
	}
	
	def "if more columns, dbunit exception"(){
		given:
			def table2_a=Mock(ITable)
			def metadata2_a=Mock(ITableMetaData)
			table2_a.getTableMetaData()>>metadata2_a
			metadata2_a.getTableName()>>"table2"
			metadata2_a.getColumns()>>[column22]
			table2_a.getRowCount()>>2
			table2_a.getValue(0,"column22")>>"0_22"
			table2_a.getValue(1,"column22")>>"1_22"
		and:
			actual=new DefaultDataSet([table1, table2].toArray(new ITable[0]))
			expected=new DefaultDataSet([table1, table2_a].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			testee.test()
		then:
			BinaryAssertionFailedException failure=thrown()
			//expected: report
			failure.getFailureContext()!=null
			failure.getFailureContext().size()==1
			failure.getFailureContext().get(0).resource instanceof FileResource
	}
	
	def "only unfiltered columns are looked for"(){
		given:
			def table2_a=Mock(ITable)
			def metadata2_a=Mock(ITableMetaData)
			table2_a.getTableMetaData()>>metadata2_a
			metadata2_a.getTableName()>>"table2"
			metadata2_a.getColumns()>>[column22]
			table2_a.getRowCount()>>2
			table2_a.getValue(0,"column22")>>"0_22"
			table2_a.getValue(1,"column22")>>"1_22"
		and:
			actual=new DefaultDataSet([table1, table2_a].toArray(new ITable[0]))
		and:
			IColumnFilter columnFilter=Mock(IColumnFilter)
			columnFilter.accept(_,"column22")>>true
		and:
			testee.addConfiguration([new DbUnitFilterResource(columnFilter)])
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			def ok=false
			testee.test()
			ok=true
		then:
			ok
	}
	
	def "if same content, ok"(){
		given:
			actual=new DefaultDataSet([table1,table2].toArray(new ITable[2]))
			actual=new DefaultDataSet([table1].toArray(new ITable[0]))
			expected=new DefaultDataSet([table1].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			def ok=false
			testee.test()
			ok=true
		then:
			ok
	}
	
	def "if different content, FAILURE, not ERROR"(){
		given:
			def table2_a=Mock(ITable)
			def metadata2_a=Mock(ITableMetaData)
			table2_a.getTableMetaData()>>metadata2_a
			metadata2_a.getTableName()>>"table2"
			metadata2_a.getColumns()>>[column21,column22]
			table2_a.getRowCount()>>2
			table2_a.getValue(0,"column21")>>"0_21"
			table2_a.getValue(0,"column22")>>"0_22"
			table2_a.getValue(1,"column21")>>"1_21_different"
			table2_a.getValue(1,"column22")>>"1_22"
		and:
			actual=new DefaultDataSet([table1, table2_a].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			def ok=false
			testee.test()
			ok=true
		then:
			BinaryAssertionFailedException failure=thrown()
			//expected: report
			failure.getFailureContext()!=null
			failure.getFailureContext().size()==1
			failure.getFailureContext().get(0).resource instanceof FileResource
	}
	
	def "if missing lines, FAILURE, not ERROR"(){
		given:
			def table2_a=Mock(ITable)
			def metadata2_a=Mock(ITableMetaData)
			table2_a.getTableMetaData()>>metadata2_a
			metadata2_a.getTableName()>>"table2"
			metadata2_a.getColumns()>>[column21,column22]
			table2_a.getRowCount()>>1
			table2_a.getValue(0,"column21")>>"0_21"
			table2_a.getValue(0,"column22")>>"0_22"
		and:
			actual=new DefaultDataSet([table1, table2_a].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			testee.test()
		then:
			BinaryAssertionFailedException failure=thrown()
			//expected: report
			failure.getFailureContext()!=null
			failure.getFailureContext().size()==1
			failure.getFailureContext().get(0).resource instanceof FileResource
	}
	
	def "if more lines, FAILURE, not ERROR"(){
		given:
			def table2_a=Mock(ITable)
			def metadata2_a=Mock(ITableMetaData)
			table2_a.getTableMetaData()>>metadata2_a
			metadata2_a.getTableName()>>"table2"
			metadata2_a.getColumns()>>[column21,column22]
			table2_a.getRowCount()>>1
			table2_a.getValue(0,"column21")>>"0_21"
			table2_a.getValue(0,"column22")>>"0_22"
		and:
			actual=new DefaultDataSet([table1, table2].toArray(new ITable[0]))
			expected=new DefaultDataSet([table1, table2_a].toArray(new ITable[0]))
		and:
			testee.setExpectedResult(new DbUnitDatasetResource(expected,false))
			testee.setActualResult(new DbUnitDatasetResource(actual,false))
		when:
			testee.test()
		then:
			BinaryAssertionFailedException failure=thrown()
			//expected: report
			failure.getFailureContext()!=null
			failure.getFailureContext().size()==1
			failure.getFailureContext().get(0).resource instanceof FileResource
	}
	
}
