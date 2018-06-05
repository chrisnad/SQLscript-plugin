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
package org.squashtest.ta.plugin.db.library.dbunit.parser

import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;
import org.squashtest.ta.plugin.db.library.dbunit.parser.ColumnFilterBuilder;
import org.squashtest.ta.plugin.db.library.dbunit.parser.FilterSaxHandler;
import org.xml.sax.Attributes;

import spock.lang.Specification;

class FilterSaxHandlerTest extends Specification{
	def testee
	def columnBuilder
	
	def setup(){
		testee=new FilterSaxHandler()
		testee.startDocument()
	}
	
	def setColumnBuilder(){
		columnBuilder=Mock(ColumnFilterBuilder)
		testee.columnFilterBuilder=columnBuilder
	}
	
	def "after passing all document, should have created a global table filter"(){
		when:
		testee.endDocument()
		def tableFilter=testee.getTableFilter()
		then:
		tableFilter!=null
	}

	def "after passing all document, should have created a global column filter"(){
		when:
		testee.endDocument()
		def columnFilter=testee.getColumnFilter()
		then:
		columnFilter!=null
	}
	
	void putStringInCurrentExpression(String value, FilterSaxHandler handler){
		char[] chars=new char[value.length()]
		value.getChars(0, value.length(), chars, 0)
		handler.characters(chars, 0, value.length())
	}
	
	def "should pass include column signal on to column builder with right table name"(){
		given:
		setColumnBuilder()
		and:
		def atts=Mock(Attributes)
		atts.getValue("","tableRegex")>>"woaAtable"
		testee.startElement("","tableInclude","tableInclude",atts)
		and:
		testee.startElement("","columnInclude","columnInclude",Mock(Attributes))
		and:
		putStringInCurrentExpression("woaColumn",testee)
		when:
		testee.endElement("","columnInclude","columnInclude")
		then:
		1 * columnBuilder.includeColumn("woaAtable","woaColumn")
	}
	
	def "should  pass exclude column signal on to column builder with right table name"(){
		given:
		setColumnBuilder()
		and:
		def atts=Mock(Attributes)
		atts.getValue("","tableRegex")>>"woaAtable"
		testee.startElement("","tableInclude","tableInclude",atts)
		and:
		testee.startElement("","columnExclude","columnExclude",Mock(Attributes))
		and:
		putStringInCurrentExpression("woaColumn",testee)
		when:
		testee.endElement("","columnExclude","columnExclude")
		then:
		1 * columnBuilder.excludeColumn("woaAtable","woaColumn")
	}
	
	def "should signal for null table when external excludeColumn"(){
		given:
		setColumnBuilder()
		and:
		def atts=Mock(Attributes)
		atts.getValue("","tableRegex")>>"woaAtable"
		testee.startElement("","tableInclude","tableInclude",atts)
		and:
		testee.endElement("","tableInclude","tableInclude")
		and:
		putStringInCurrentExpression("woaColumn",testee)
		when:
		testee.endElement("","columnExclude","columnExclude")
		then:
		1 * columnBuilder.excludeColumn(null,"woaColumn")
	}
	
	def "should build table name for exclude"(){		
		given:
		setColumnBuilder()
		and:
		def atts=Mock(Attributes)
		atts.getValue("","tableRegex")>>"woaAtable"
		testee.startElement("","tableExclude","tableExclude",atts)
		when:
		testee.endElement("","tableExclude","tableExclude")
		def tableFilter=testee.getTableFilter()
		then:
		tableFilter.accept("woaAtable")==false
	}
	
	def "should detect filterPK=true"(){
		given:
		setColumnBuilder()
		and:
		def atts=Mock(Attributes)
		testee.startElement("","filterOutPKs","filterOutPKs",atts)
		when:
		testee.endElement("","filterOutPKs","filterOutPKs")
		then:
		testee.hasHidePk()
	}
	
	def "should detect filterPK=false"(){
		given:
		setColumnBuilder()
		when:
		def hasPk=testee.hasHidePk()
		then:
		hasPk==false
	}
	
	def "should detect filterOutTimestamps=true and go for default"(){
		given:
		setColumnBuilder()
		and:
		def atts=Mock(Attributes)
		testee.startElement("","filterOutTimestamps","filterOutTimestamps",atts)
		and:
		putStringInCurrentExpression("true",testee)
		when:
		testee.endElement("","filterOutTimestamps","filterOutTimestamps")
		then:
		//calls addTimestampExcludeFilter with empty set ==> default value
		1 * columnBuilder.addTimestampExcludeFilter({
			it instanceof Collection<String>
			it.size()==0
		})
	}
	
	def "should detect filterOutTimestamps=true and pass on specified types if any"(){
		given:
		setColumnBuilder()
		and:
		def atts=Mock(Attributes)
		testee.startElement("","filterOutTimestamps","filterOutTimestamps",atts)
		and:
		testee.startElement("","time","time",atts)
		testee.startElement("","date","date",atts)
		when:
		testee.endElement("","filterOutTimestamps","filterOutTimestamps")
		then:
		//calls addTimestampExcludeFilter with empty set ==> default value
		1 * columnBuilder.addTimestampExcludeFilter({
			it instanceof Collection<String>
			new HashSet<String>(["time","date"]).equals(new HashSet<String>(it))
		})
	}
	
	def "should detect filterOutTimestamps=false"(){
		given:
		setColumnBuilder()
		and:
		def atts=Mock(Attributes)
		testee.startElement("","filterOutTimestamps","filterOutTimestamps",atts)
		and:
		putStringInCurrentExpression("false",testee)
		when:
		testee.endElement("","filterOutTimestamps","filterOutTimestamps")
		then:
		0 * columnBuilder.addTimestampExcludeFilter()
	}
}
