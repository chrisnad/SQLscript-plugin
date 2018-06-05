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
package org.squashtest.ta.plugin.db.library.sql

import java.sql.DatabaseMetaData
import java.sql.ResultSet

import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.dbunit.dataset.xml.FlatXmlProducer
import org.squashtest.ta.plugin.db.library.sql.DatabaseMetadataExplorer;
import org.xml.sax.InputSource

import spock.lang.Specification

class DatabaseMetadataFactoryTest extends Specification {
	DatabaseMetadataExplorer testee
	DatabaseMetaData metadata
	def setup(){
		metadata=Mock()
		testee=new DatabaseMetadataExplorer(metadata, null);
	}
	
	def "pks in the result set order if no pk index"(){
		given:
			FlatXmlProducer producer=new FlatXmlProducer(new InputSource(getClass().getResourceAsStream("metadataNoPkIndex.xml")))
			IDataSet data=new FlatXmlDataSet(producer)
			ITable pkTable=data.getTable("knownTable")
		and:
			def tableName = "knownTable"
			metadata.getPrimaryKeys(_, _, tableName)>>new MockDbunitResultSet(pkTable)
		when:
			List<String> pks=testee.getPrimaryKeyNames(tableName);
		then:
			pks.equals(["ONE","ANOTHER"])
	}
	
	def "pks in the result set order "(){
		given:
			ResultSet primaryKeysRs=Mock()
			primaryKeysRs.next()>>>[true,true,false]
			primaryKeysRs.getInt(_)>>>[2,1]
			primaryKeysRs.getRow()>>>[1,2]
			primaryKeysRs.getString(_)>>>["ONE","ANOTHER"]
		and:
			def tableName = "knownTable"
			metadata.getPrimaryKeys(_, _, tableName)>>primaryKeysRs
		when:
			List<String> pks=testee.getPrimaryKeyNames(tableName);
		then:
			pks.equals(["ANOTHER","ONE"])
	}
}
