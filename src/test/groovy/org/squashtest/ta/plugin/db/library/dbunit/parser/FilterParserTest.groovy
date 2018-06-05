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

import org.squashtest.ta.framework.exception.BadDataException
import org.squashtest.ta.plugin.db.library.dbunit.parser.FilterParser.FilterErrorHandlerFactory
import org.squashtest.ta.plugin.db.library.dbunit.parser.FilterParser.FilterSaxHandlerFactory
import org.xml.sax.ContentHandler
import org.xml.sax.ErrorHandler

import spock.lang.Specification

class FilterParserTest extends Specification{
	def testee
	def handler
	def errorHanlder
	
	def setup(){
		testee=new FilterParser()
		handler=Mock(ContentHandler)
		errorHanlder=Mock(ErrorHandler)
		def handlerFactory=Mock(FilterSaxHandlerFactory)
		def errorHandlerFactory=Mock(FilterErrorHandlerFactory)
		handlerFactory.newInstance()>>handler
		errorHandlerFactory.newInstance()>>errorHanlder
	}
	
	def "valid document based on table exclusion should be accepted"(){
		given:
			def stream=getClass().getResourceAsStream("validExcludingFilter.xml")
		when:
			testee.parse(stream)
		then:
			true
	}
	
	def "valid document based on table inclusion should be accepted"(){
		given:
			def stream=getClass().getResourceAsStream("validExcludingFilter.xml")
		when:
			testee.parse(stream)
		then:
			true
	}
	
	def "document with mixed table include & exclude should be rejected"(){
		given:
		def stream=getClass().getResourceAsStream("invalidMixedFilter.xml")
	when:
		testee.parse(stream)
	then:
		thrown BadDataException
	}
	
	def "document with mixed column include & exclude in same table should be rejected"(){
		given:
		def stream=getClass().getResourceAsStream("invalidMixedFilter.xml")
	when:
		testee.parse(stream)
	then:
		thrown BadDataException
	}
	
	def "document with global include column should be rejected"(){
		given:
		def stream=getClass().getResourceAsStream("invalidGlobalColumnFilter.xml")
	when:
		testee.parse(stream)
	then:
		thrown BadDataException
	}
	
	def "invalid datatype in timestamp filter should be rejected"(){
		given:
		def stream=getClass().getResourceAsStream("invalidTimestampDataType.xml")
	when:
		testee.parse(stream)
	then:
		thrown BadDataException
	}
}
