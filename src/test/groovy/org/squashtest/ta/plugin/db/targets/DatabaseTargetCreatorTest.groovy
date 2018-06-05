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
package org.squashtest.ta.plugin.db.targets

import spock.lang.Specification;

class DatabaseTargetCreatorTest extends Specification {
	
	DatabaseTargetCreator testee
	
	def setup(){
		testee = new DatabaseTargetCreator()
	}
	
	def "target with shebang can be instantiated"(){
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/properties/db.properties")
		when :
			def res = testee.canInstantiate(url)
		then :
			res == true
	}
	
	def "target without shebang can be instantiated"(){
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/properties/db_withoutShebang.properties")
		when :
			def res = testee.canInstantiate(url)
		then :
			res == true
	}
	
	def "other targets cannot be instantiated"(){
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/properties/http.properties")
		when :
			def res = testee.canInstantiate(url)
		then :
			res == false
	}

	def "target has correct keys"(){
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/properties/db_withoutShebang.properties")
			File file = new File(url.toURI())
		when :
			def res = testee.checkKeys(file)
			
		then :
			res == true
	}
	
	def "target has incorrect keys"(){
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/properties/db_incorrect.properties")
			File file = new File(url.toURI())
		when :
			def res = testee.checkKeys(file)
			
		then :
			res == false
	}
	
	def "target with shebang can be created"(){
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/properties/db.properties")
		when :
			def target = testee.createTarget(url)
		then :
			target instanceof DatabaseTarget
			Properties prop = target.getConfiguration()
			prop.getProperty("squashtest.ta.database.driver") == "com.mysql.jdbc.Driver"
			prop.getProperty("squashtest.ta.database.url") == "jdbc:mysql://test"
	}
	
	def "target without shebang can be created"(){
		given :
			URL url = getClass().getClassLoader().getResource("org/squashtest/ta/plugin/db/properties/db_withoutShebang.properties")
		when :
			def target = testee.createTarget(url)
			
		then :
			target instanceof DatabaseTarget
			Properties prop = target.getConfiguration()
			prop.getProperty("squashtest.ta.database.driver") == "com.mysql.jdbc.Driver"
			prop.getProperty("squashtest.ta.database.url") == "jdbc:mysql://test"		
	}
}
