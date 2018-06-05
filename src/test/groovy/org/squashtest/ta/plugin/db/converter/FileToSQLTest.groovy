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
import org.squashtest.ta.framework.components.Resource
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.framework.exception.IllegalConfigurationException;
import org.squashtest.ta.plugin.db.converter.FileToSQL;
import org.squashtest.ta.plugin.db.resources.SQLQuery;

import spock.lang.Specification

class FileToSQLTest extends Specification {

	def "the query in the file must be correctly translated"(){
	
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/FileToSQLResource.sql");
			File file = new File(url.toURI())
			FileResource myResource = new FileResource(file)
			FileToSQL converter = new FileToSQL();
		when :
			SQLQuery myQuery = converter.convert(myResource);
			
		then :
			myQuery.getQuery().equals("select * from A_GIVEN_TABLE where 1=1;"); 
	}
        
        def "the query in the ISO_8858_1 file must be correctly translated"(){
	
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/FileToSQLResource_ISO_8859_1.sql");
			File file = new File(url.toURI())
			FileResource myResource = new FileResource(file)
                        
                        URL confURL = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/options/ISO_8859_1.opts");
                        File confFile = new File(confURL.toURI())
			FileResource myConfResource = new FileResource(confFile)
                        List<Resource<?>> configuration = new ArrayList<Resource<?>>()
                        configuration.add(myConfResource)
			FileToSQL converter = new FileToSQL()
                        converter.addConfiguration(configuration)
		when :
			SQLQuery myQuery = converter.convert(myResource);
			
		then :
			myQuery.getQuery().equals("select * from A_GIVEN_TABLE where stringparam='accentué';"); 
	}
        
        def "the query in the UTF-8 file must be correctly translated"(){
	
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/FileToSQLResource_UTF_8.sql");
			File file = new File(url.toURI())
			FileResource myResource = new FileResource(file)
                        
                        URL confURL = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/options/UTF_8.opts");
                        File confFile = new File(confURL.toURI())
			FileResource myConfResource = new FileResource(confFile)
                        List<Resource<?>> configuration = new ArrayList<Resource<?>>()
                        configuration.add(myConfResource)
			FileToSQL converter = new FileToSQL()
                        converter.addConfiguration(configuration)
		when :
			SQLQuery myQuery = converter.convert(myResource);
			
		then :
			myQuery.getQuery().equals("select * from A_GIVEN_TABLE where stringparam='accentué';"); 
	}
        
       def "should fail because of unknown encoding"(){
		
		given :
                        URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/FileToSQLResource.sql");
			File file = new File(url.toURI())
			FileResource myResource = new FileResource(file)
                        
                        URL confURL = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/options/UNKNOWN-ENCODING.opts");
                        File confFile = new File(confURL.toURI())
			FileResource myConfResource = new FileResource(confFile)
                        List<Resource<?>> configuration = new ArrayList<Resource<?>>()
                        configuration.add(myConfResource)
			FileToSQL converter = new FileToSQL()
                        converter.addConfiguration(configuration)
		when :
                        SQLQuery myQuery = converter.convert(myResource);
		then :
			thrown(IllegalConfigurationException)
        }
 
        def "should fail because the file has two queries"(){
		
			given :
				URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/converter/FileToSQLResource_KO.sql");
				File file = new File(url.toURI())
				FileResource myResource = new FileResource(file)
				FileToSQL converter = new FileToSQL();
			when :
				converter.convert(myResource);
				
			then :
			thrown(BadDataException)
		}
}
