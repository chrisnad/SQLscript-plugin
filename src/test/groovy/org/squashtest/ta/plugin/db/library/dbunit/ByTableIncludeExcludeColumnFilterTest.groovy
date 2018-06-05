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
import org.squashtest.ta.plugin.db.library.dbunit.ByTableIncludeExcludeColumnFilter;

import spock.lang.Specification

class ByTableIncludeExcludeColumnFilterTest extends Specification {
	def "should exclude a table when its table  & name matche a defined exclusion"(){
		given:
			def filter=new ByTableIncludeExcludeColumnFilter()
		and:
			filter.addColumnExcludeFilter("toto", "unwanted")
		and:
			def column=Mock(Column)
			column.getColumnName()>>"unwanted"
		when:
			def result=filter.accept("toto", column)
		then:
			!result		
	}
}
