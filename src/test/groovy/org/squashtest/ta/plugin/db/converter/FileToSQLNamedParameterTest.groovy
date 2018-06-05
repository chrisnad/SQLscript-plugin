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
package org.squashtest.ta.plugin.db.converter;

import static org.junit.Assert.*

import org.squashtest.ta.framework.components.FileResource
import org.squashtest.ta.framework.exception.BadDataException
import org.squashtest.ta.plugin.db.converter.FileToSQLNamedParameters;
import org.squashtest.ta.plugin.db.resources.SQLNamedParameters;

import spock.lang.Specification

class FileToSQLNamedParameterTest extends Specification {

	def "the parameters are correctly maped"(){
	
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/FileToSQLNamedParametersResource.sql")
			File file = new File(url.toURI())
			FileResource myResource = new FileResource(file)
			FileToSQLNamedParameters converter = new FileToSQLNamedParameters()
		when :
			SQLNamedParameters myParams = converter.convert(myResource)
			
		then :
			myParams.getParam("landlord") == "'toto'"
		expect : 
			myParams.getValues().get(i).getName() == paramName
			myParams.getValues().get(i).getValue() == paramValue
		where : 
			i | paramName			| paramValue
			0 | "landlord"			| "'toto'"
			1 | "nb_inhabitants"	| "20"
			2 | "renter"			| "'tata'"
	}
	
	def "the conversion should crash"(){
	
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/FileToSQLParametersBadResource.sql")
			File file = new File(url.toURI())
			FileResource myResource = new FileResource(file)
			FileToSQLNamedParameters converter = new FileToSQLNamedParameters()
		when :
			SQLNamedParameters myParams = converter.convert(myResource)
			
		then :
			thrown BadDataException
	}
}
