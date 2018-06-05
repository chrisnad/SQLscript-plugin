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
package org.squashtest.ta.plugin.db.library.dbunit.parser

import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;
import org.squashtest.ta.plugin.db.library.dbunit.parser.ColumnFilterBuilder;

import spock.lang.Specification;

class ColumnFilterBuilderTest extends Specification{
	def testee
	
	def setup(){
		testee=new ColumnFilterBuilder()
	}
	
	def "basic generated filter should exist"(){
		when:
			def filter=testee.getColumnFilter()
		then:
			filter!=null
			filter.accept("",Mock(Column))//should take anything
	}
	
	def "should honor addTimestampExcludeFilter([]) excluding TIMESTAMP alone"(){
		given:
			def tsColumn=Mock(Column)
			tsColumn.getDataType()>>DataType.TIMESTAMP
		and:
			def noTsColumn=Mock(Column)
			noTsColumn.getDataType()>>DataType.CLOB
			noTsColumn.isTimeStamp()>>false
		when:
			testee.addTimestampExcludeFilter([])
		then:
			def filter=testee.getColumnFilter()
			filter.accept("",noTsColumn)
			!filter.accept("",tsColumn)
	}

	def "should honor addTimestampExcludeFilter([time]) excluding TIME alone"(){
		given:
			def tsColumn=Mock(Column)
			tsColumn.getDataType()>>DataType.TIME
		and:
		and:
			def noTsColumn=Mock(Column)
			noTsColumn.getDataType()>>DataType.CLOB
			noTsColumn.isTimeStamp()>>false
			def passingTsColumn=Mock(Column)
			passingTsColumn.getDataType()>>DataType.TIMESTAMP
			passingTsColumn.isTimeStamp()>>false
		when:
			testee.addTimestampExcludeFilter(["time"])
		then:
			def filter=testee.getColumnFilter()
			filter.accept("",noTsColumn)
			filter.accept("",passingTsColumn)
			!filter.accept("",tsColumn)
	}

	def "should honor addTimestampExcludeFilter([time,date]) excluding TIME and DATE"(){
		given:
			def tsColumn=Mock(Column)
			tsColumn.getDataType()>>DataType.TIME
			def tsColumn2=Mock(Column)
			tsColumn2.getDataType()>>DataType.DATE
		and:
		and:
			def noTsColumn=Mock(Column)
			noTsColumn.getDataType()>>DataType.CLOB
			noTsColumn.isTimeStamp()>>false
			def passingTsColumn=Mock(Column)
			passingTsColumn.getDataType()>>DataType.TIMESTAMP
			passingTsColumn.isTimeStamp()>>false
		when:
			testee.addTimestampExcludeFilter(["time","date"])
		then:
			def filter=testee.getColumnFilter()
			filter.accept("",noTsColumn)
			filter.accept("",passingTsColumn)
			!filter.accept("",tsColumn)
			!filter.accept("",tsColumn2)
	}

	
	def "should exclude said columns from any table when no other filter"(){
		given:
			def excludedColumn=Mock(Column)
			excludedColumn.getColumnName()>>"unwanted"
		and:
			def otherColumn=Mock(Column)
			otherColumn.getColumnName()>>"other"
		when:
			testee.excludeColumn(null, "unwanted")
		then:
			def filter=testee.getColumnFilter()
			filter.accept("toto",otherColumn)
			filter.accept("tutu",otherColumn)
			!filter.accept("toto",excludedColumn)
			!filter.accept("tutu",excludedColumn)
	}
	
	def "should exclude said columns from said table when no other filter"(){
		given:
			def excludedColumn=Mock(Column)
			excludedColumn.getColumnName()>>"unwanted"
		and:
			def otherColumn=Mock(Column)
			otherColumn.getColumnName()>>"other"
		when:
			testee.excludeColumn("toto", "unwanted")
		then:
			def filter=testee.getColumnFilter()
			filter.accept("toto",otherColumn)
			filter.accept("tutu",otherColumn)
			!filter.accept("toto",excludedColumn)
			filter.accept("tutu",excludedColumn)
	}
}
