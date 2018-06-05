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
package org.squashtest.ta.plugin.db.library.dbunit.helper

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.datatype.DataType;

import spock.lang.Specification

class AugmentedTableTest extends Specification {
	def table
	def col
	def testee
	
	def setup(){
		col=Mock(Column)
		col.getColumnName()>>"Col1"
		col.getDataType()>> DataType.VARCHAR
		col.getSqlTypeName()>>"VARCHAR"
		col.equals(_)>>false
		col.equals(col)>>true
		table=new DefaultTable("Toto",[col].toArray(new Column[1]))
		table.addRow("First")
		table.addRow("Second")
		
		testee=new AugmentedTable(table)
	}
	
	def "can read original rows before adding new ones"(){
		when:
			def originalRows=[testee.getValue(0,col.getColumnName()),testee.getValue(1,col.getColumnName())]
		then:
			"First".equals(originalRows.get(0))
			"Second".equals(originalRows.get(1))
	}
	
	def "can read original rows after adding new ones"(){
		given:
			testee.registerValue(2,col.getColumnName(),"Third")
			testee.registerValue(2,col.getColumnName(),"Fourth")
		when:
			def originalRows=[testee.getValue(0,col.getColumnName()),testee.getValue(1,col.getColumnName())]
		then:
			"First".equals(originalRows.get(0))
			"Second".equals(originalRows.get(1))
	}
	
	def "can read new rows after adding them"(){
		given:
			testee.registerValue(2,col.getColumnName(),"Third")
			testee.registerValue(3,col.getColumnName(),"Fourth")
		when:
			def newRows=[testee.getValue(2,col.getColumnName()),testee.getValue(3,col.getColumnName())]
		then:
			"Third".equals(newRows.get(0))
			"Fourth".equals(newRows.get(1))
	}
}
