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
import org.dbunit.dataset.filter.IColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.CompositeColumnFilter;

import spock.lang.Specification

class CompositeColumnFilterTest extends Specification {
	def testee
	def "should only accept columns that pass all filters (table changes)"(){
		given:
			def filter1=Mock(IColumnFilter)
			def filter2=Mock(IColumnFilter)
			def filters=[filter1,filter2]
		and:
			def col1=Mock(Column)
		and:
			filter1.accept("Table1",_)>>false
			filter1.accept("Table2",_)>>false
			filter1.accept("Table3",_)>>true
			filter1.accept("Table4",_)>>true
		and:
			filter2.accept("Table1",_)>>false
			filter2.accept("Table2",_)>>true
			filter2.accept("Table3",_)>>false
			filter2.accept("Table4",_)>>true
		and:
			testee=new CompositeColumnFilter(filters.toArray(new IColumnFilter[0]))
		when:
			def r1=testee.accept("Table1",col1)
			def r2=testee.accept("Table2",col1)
			def r3=testee.accept("Table3",col1)
			def r4=testee.accept("Table4",col1)
		then:
			r1==false
			r2==false
			r3==false
			r4==true
	}
	
	def "should only accept columns that pass all filters (column changes)"(){
		given:
			def filter1=Mock(IColumnFilter)
			def filter2=Mock(IColumnFilter)
			def filters=[filter1,filter2]
		and:
			def col1=Mock(Column)
			def col2=Mock(Column)
			def col3=Mock(Column)
			def col4=Mock(Column)
		and:
			filter1.accept(_,col1)>>false
			filter1.accept(_,col2)>>false
			filter1.accept(_,col3)>>true
			filter1.accept(_,col4)>>true
		and:
			filter2.accept(_,col1)>>false
			filter2.accept(_,col2)>>true
			filter2.accept(_,col3)>>false
			filter2.accept(_,col4)>>true
		and:
			testee=new CompositeColumnFilter(filters.toArray(new IColumnFilter[0]))
		when:
			def r1=testee.accept("Table1",col1)
			def r2=testee.accept("Table1",col2)
			def r3=testee.accept("Table1",col3)
			def r4=testee.accept("Table1",col4)
		then:
			r1==false
			r2==false
			r3==false
			r4==true
	}
}
