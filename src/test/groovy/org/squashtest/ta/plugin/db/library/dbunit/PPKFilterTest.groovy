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
import org.squashtest.ta.plugin.db.library.dbunit.PPKFilter;

import spock.lang.Specification;

class PPKFilterTest extends Specification{
	Properties data
	PPKFilter testee
	
	def setup(){
		data=new Properties()
	}
	
	def "should find single column PPK in target table ok"(){
		given:
			data.setProperty("targetTable", "toto")
		and:
			Column col=Mock()
			col.getColumnName()>>"toto"
		when:
			testee=new PPKFilter(data)
		then:
			testee.accept("targetTable", col)
	}
	
	def "should not tag non-pk column in single-pk target table"(){
		given:
			data.setProperty("targetTable", "toto")
		and:
			Column col=Mock()
			col.getColumnName()>>"not_pk"
		when:
			testee=new PPKFilter(data)
		then:
			!testee.accept("targetTable", col)
	}
	
	def "should not tag same name column of antoher table than target"(){
		given:
			data.setProperty("targetTable", "toto")
			data.setProperty("not-targetTable", "otherpk")
		and:
			Column col=Mock()
			col.getColumnName()>>"toto"
		when:
			testee=new PPKFilter(data)
		then:
			!testee.accept("not-targetTable", col)
	}

	def "should find multi-column PPK in target table ok"(){
		given:
			data.setProperty("targetTable", "toto,sidekick")
		and:
			Column col=Mock()
			col.getColumnName()>>"toto"
		and:
			Column col2=Mock()
			col2.getColumnName()>>"sidekick"
		when:
			testee=new PPKFilter(data)
		then:
			testee.accept("targetTable", col)
			testee.accept("targetTable", col2)
	}
	
	def "should not tag non-pk column in multi-column-pk target table"(){
		given:
			data.setProperty("targetTable", "toto,sidekick")
		and:
			Column col=Mock()
			col.getColumnName()>>"not_pk"
		when:
			testee=new PPKFilter(data)
		then:
			!testee.accept("targetTable", col)
	}
	
	def "should not tag same name column of antoher table than target (multi-column pk)"(){
		given:
			data.setProperty("targetTable", "toto,sidekick")
			data.setProperty("not-targetTable", "TheBrain,Pinky")
		and:
			Column col=Mock()
			col.getColumnName()>>"toto"
		when:
			testee=new PPKFilter(data)
		then:
			!testee.accept("not-targetTable", col)
	}
	
	def "column recognition should be case-insensisive (single-pk)"(){
		given:
			data.setProperty("table", "TheBrain")
		and:
			Column colMin=Mock()
			colMin.getColumnName()>>"thebrain"
		and:
			Column colMaj=Mock()
			colMaj.getColumnName()>>"THEBRAIN"
		and:
			Column colMix=Mock()
			colMix.getColumnName()>>"TheBrain"
		and:
			Column colOtherMix=Mock()
			colOtherMix.getColumnName()>>"tHebraIn"
		when:
			testee=new PPKFilter(data)
		then:
			testee.accept("table", colMin)
			testee.accept("table", colMaj)
			testee.accept("table", colMix)
			testee.accept("table", colOtherMix)
	}
	
	def "column recognition should be case-insensisive (multi-column-pk)"(){
		given:
		data.setProperty("table", "Pinky,TheBrain")
	and:
		Column colMin=Mock()
		colMin.getColumnName()>>"thebrain"
	and:
		Column colMaj=Mock()
		colMaj.getColumnName()>>"THEBRAIN"
	and:
		Column colMix=Mock()
		colMix.getColumnName()>>"TheBrain"
	and:
		Column colOtherMix=Mock()
		colOtherMix.getColumnName()>>"tHebraIn"
	when:
		testee=new PPKFilter(data)
	then:
		testee.accept("table", colMin)
		testee.accept("table", colMaj)
		testee.accept("table", colMix)
		testee.accept("table", colOtherMix)
	}
}
