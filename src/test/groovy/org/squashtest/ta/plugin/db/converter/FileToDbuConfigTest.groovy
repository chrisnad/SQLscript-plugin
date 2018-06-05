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

import org.dbunit.database.DatabaseConfig
import org.squashtest.ta.framework.components.FileResource
import org.squashtest.ta.framework.exception.BadDataException
import org.squashtest.ta.plugin.db.converter.FileToDbuConfig;
import org.squashtest.ta.plugin.db.resources.DbUnitConfiguration;

import spock.lang.Specification

class FileToDbuConfigTest extends Specification {

	def "the properties file is correctly configured"(){
	
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/library/dbunit/DatabaseConfig.properties")
			File file = new File(url.toURI())
			FileResource myFileResource = new FileResource(file)
			FileToDbuConfig converter = new FileToDbuConfig()
		when :
			DbUnitConfiguration myResource = converter.convert(myFileResource)
			DatabaseConfig dbconf = myResource.getConfiguration()
		then :
			notThrown BadDataException
	}
	
	def "the conversion should crash"(){
		
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/library/dbunit/BadDatabaseConfig.properties")
			File file = new File(url.toURI())
			FileResource myFileResource = new FileResource(file)
			FileToDbuConfig converter = new FileToDbuConfig()
		when :
			DbUnitConfiguration myResource = converter.convert(myFileResource)
			
		then :
			thrown BadDataException
	}
}
