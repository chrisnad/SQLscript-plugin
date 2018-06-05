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

import org.dbunit.dataset.filter.ITableFilter;
import org.squashtest.ta.plugin.db.library.dbunit.CompositeTableFilter;

import spock.lang.Specification

class CompositeTableFilterTest extends Specification {
	def testee;
	def "should accept only table that pass all elementary filters"(){
		given:
			def filter1=Mock(ITableFilter)
			def filter2=Mock(ITableFilter)
			def tableFilters=[filter1,filter2]
		and:
			filter1.accept("Table1")>>true
			filter1.accept("Table2")>>false
			filter1.accept("Table3")>>true
			filter1.accept("Table4")>>false
		and:
			filter2.accept("Table1")>>false
			filter2.accept("Table2")>>true
			filter2.accept("Table3")>>true
			filter2.accept("Table4")>>false
		and:
			testee=new CompositeTableFilter(tableFilters)
		when:
			def r1=testee.accept("Table1")
			def r2=testee.accept("Table2")
			def r3=testee.accept("Table3")
			def r4=testee.accept("Table4")
		then:
			r1==false
			r2==false
			r3==true
			r4==false
	}
}
