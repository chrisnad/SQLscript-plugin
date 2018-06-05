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
package org.squashtest.ta.plugin.db.library.sql.query

import org.squashtest.ta.plugin.db.library.sql.SQLParamUtil;

import spock.lang.Specification

class SQLParamUtilTest extends Specification {

	/* ****************** test : POSITIONAL *************** */
	
	def "POSITIONAL : should say that a query is positional param based"(){
		
		given :
			def sql = "select bob from MIKE where robert = ?"
		
		when :
			def res = SQLParamUtil.POSITIONAL.match(sql);
		
		then :
			res == true
		
	}
	
	def "POSITIONAL : should say that a query is not positional param based"(){
		
		given :
			def sql = "select bob from MIKE where robert = :ted"
		
		when :
			def res = SQLParamUtil.POSITIONAL.match(sql);
		
		then :
			res == false
		
	}
	
	
	def "POSITIONAL : should return the next parameter name"(){
		given :
			def sql = "select bob from MIKE where robert =	\t	?"
		
		when :
			def res = SQLParamUtil.POSITIONAL.findNextParamName(sql);
		
		then :
			res == "?"
		
	}
	
	
	def "POSITIONAL : should return null because there are no more parameters to set"(){
		given :
			def sql = "select bob from MIKE where robert = 'ted'"
		
		when :
			def res = SQLParamUtil.POSITIONAL.findNextParamName(sql);
		
		then :
			res == null
		
	}
	
	def "POSITIONAL : should replace the next param with the given value"(){
		given :
			def sql = "select bob from MIKE where robert = ?"
		
		when :
			def res = SQLParamUtil.POSITIONAL.replaceNext(sql, "'ted'")
		
		then :
			res == "select bob from MIKE where robert = 'ted'"
	}
	
	def "POSITIONAL : should return the unmodified SQL string since all params were set"(){
		given :
			def sql = "select bob from MIKE where robert = 'ted'"
		
		when :
			def res = SQLParamUtil.POSITIONAL.replaceNext(sql, "gary")
		
		then :
			res == "select bob from MIKE where robert = 'ted'"		
	}
	
	def "POSITIONAL : should replace the next param even when a specified param is provided"(){
		given :
			def sql = "select bob from MIKE where robert = ?"
		
		when :
			def res = SQLParamUtil.POSITIONAL.replaceSpecific(sql, "uh","'ted'")
		
		then :
			res == "select bob from MIKE where robert = 'ted'"
		
	}
	
	/* ****************** test : NAMED *************** */
	
	def "NAMED : should say that a query is named param based"(){
		
		given :
			def sql = "select bob from MIKE where robert = :ted"
		
		when :
			def res = SQLParamUtil.NAMED.match(sql);
		
		then :
			res == true
		
	}
	
	def "NAMED : should say that a query is not named param based"(){
		
		given :
			def sql = "select bob from MIKE where robert = ?"
		
		when :
			def res = SQLParamUtil.NAMED.match(sql);
		
		then :
			res == false
		
	}
	
	
	def "NAMED : should return the next parameter name"(){
		given :
			def sql = "select bob from MIKE where robert =	\t	:ted"
		
		when :
			def res = SQLParamUtil.NAMED.findNextParamName(sql);
		
		then :
			res == "ted"
		
	}
	
	
	def "NAMED : should return null because there are no more parameters to set"(){
		given :
			def sql = "select bob from MIKE where robert = 'ted'"
		
		when :
			def res = SQLParamUtil.NAMED.findNextParamName(sql);
		
		then :
			res == null
		
	}
	
	def "NAMED : should replace the next param with the given value"(){
		given :
			def sql = "select bob from MIKE where robert = :gary"
		
		when :
			def res = SQLParamUtil.NAMED.replaceNext(sql, "'ted'")
		
		then :
			res == "select bob from MIKE where robert = 'ted'"
	}
	
	def "NAMED : should return the unmodified SQL string since all params were set"(){
		given :
			def sql = "select bob from MIKE where robert = 'ted'"
		
		when :
			def res = SQLParamUtil.NAMED.replaceNext(sql, "gary")
		
		then :
			res == "select bob from MIKE where robert = 'ted'"
	}
	
	def "NAMED : should replace the specified param"(){
		given :
			def sql = "select bob from MIKE where rudolph = :jay  and robert = :gary"
		
		when :
			def res = SQLParamUtil.NAMED.replaceSpecific(sql, "gary","'ted'")
		
		then :
			res == "select bob from MIKE where rudolph = :jay  and robert = 'ted'"
		
	}
	
	/* ****************** test : NONE *************** */
	
	def "NONE : should say that a query has an unknown parameterizing system, or expects no parameters"(){
		
		given :
			def sql = "select bob from MIKE where robert = 'ted'"
		
		when :
			def res = SQLParamUtil.NONE.match(sql);
		
		then :
			res == true
		
	}

	
	def "NONE : should return null because there are no parameters to find"(){
		given :
			def sql = "select bob from MIKE where robert =	'ted'"
		
		when :
			def res = SQLParamUtil.NONE.findNextParamName(sql);
		
		then :
			res == null
		
	}
	

	def "NONE : should return the unmodified SQL string since all params were set"(){
		given :
			def sql = "select bob from MIKE where robert = 'ted'"
		
		when :
			def res = SQLParamUtil.NONE.replaceNext(sql, "gary")
		
		then :
			res == "select bob from MIKE where robert = 'ted'"
	}
	
	def "NONE : should return the unmodified SQL string since all params were set (2)"(){
		given :
			def sql = "select bob from MIKE where robert = 'ted'"
		
		when :
			def res = SQLParamUtil.NONE.replaceSpecific(sql, "gary","'ted'")
		
		then :
			res == "select bob from MIKE where robert = 'ted'"
		
	}
	
}
