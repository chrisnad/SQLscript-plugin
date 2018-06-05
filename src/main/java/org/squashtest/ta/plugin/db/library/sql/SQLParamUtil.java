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
package org.squashtest.ta.plugin.db.library.sql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SQLParamUtil {

	POSITIONAL(){
		@Override
		public boolean match(String sql) {
			Matcher matcher = POSITIONAL_PARAM_FIND_PATTERN.matcher(sql);
			return matcher.find();
		}
		
		//returns "?" since for positional queries '?' is the only parameter name... or null if not found of course.
		@Override
		public String findNextParamName(String sql) {
			return (match(sql)) ? "?" : null;
		}
	
		
		@Override
		public String replaceNext(String sql, String param) {
			
			String result = sql;
			Matcher matcher = POSITIONAL_PARAM_REPLACE_PATTERN.matcher(sql);
			boolean found = matcher.find();
			
			if (found){
				result = matcher.replaceFirst(param);
			}
			
			return result;
		}

		@Override
		public String replaceSpecific(String sql, String paramName, String param) {
			return replaceNext(sql, param);
		}
		
	},
	NAMED(){
		@Override
		public boolean match(String sql) {
			Matcher matcher = NAMED_PARAM_FIND_PATTERN.matcher(sql);
			return matcher.find();
		}
		
		@Override
		public String findNextParamName(String sql) {
			Matcher matcher = NAMED_PARAM_FIND_PATTERN.matcher(sql);
			boolean found = matcher.find();
			if (found){
				return matcher.group(2);
			}else{
				return null;
			}
		}
		
		@Override
		public String replaceNext(String sql, String param) {
			
			String result = sql;
			Matcher matcher = NAMED_PARAM_REPLACE_NEXT_PATTERN.matcher(sql);
			boolean found = matcher.find();
			
			if (found){
				result = matcher.replaceFirst(param);
			}
			
			return result;
		}
		
		@Override
		public String replaceSpecific(String sql, String paramName, String replacement){
			
			String result = sql;
			
			String regexp = NAMED_PARAM_REPLACE_NAMED_STRING.replace("##replaceme##", paramName);
			Matcher matcher = Pattern.compile(regexp).matcher(sql);
			
			boolean found = matcher.find();
			if (found){
				result = matcher.replaceFirst(replacement);
			}
			
			return result;
			
		}
		
		
	},
	NONE(){
		@Override
		public boolean match(String sql) {
			return ! ( (POSITIONAL.match(sql)) || 
					   (NAMED.match(sql))
					 );
		}
		
		@Override
		public String findNextParamName(String sql) {
			return null;
		}
		
		@Override
		public String replaceNext(String sql, String param) {
			return sql;
		}
		
		@Override
		public String replaceSpecific(String sql, String paramName, String param) {
			return sql;
		}
		
	};

	private static final Pattern POSITIONAL_PARAM_FIND_PATTERN = Pattern.compile("=\\s*(\\?)");
	private static final Pattern POSITIONAL_PARAM_REPLACE_PATTERN = Pattern.compile("(?<=\\s*)(\\?)");
	
	private static final Pattern NAMED_PARAM_FIND_PATTERN = Pattern.compile("=\\s*(:([\\w_-]+))"); 
	private static final Pattern NAMED_PARAM_REPLACE_NEXT_PATTERN = Pattern.compile("(?<=\\s*)(:[\\w_-]+)");
	private static final String NAMED_PARAM_REPLACE_NAMED_STRING = "(?<=\\s*)(:##replaceme##)";
	
	
	public static SQLParamUtil whichStyle(String sql){
		SQLParamUtil sqlParamUtil = NONE;
		if (POSITIONAL.match(sql)){
			sqlParamUtil = POSITIONAL;
		}else if (NAMED.match(sql)){
			sqlParamUtil = NAMED;
		}
		return sqlParamUtil;
	}
	
	public abstract boolean match(String sql);
	
	/**
	 * This is no iterator method, it just return the next param left unset. If there is at least one param left to set, it will return its name. If there is none, it will
	 * return null.
	 * 
	 * @param sql
	 * @return
	 */
	public abstract String findNextParamName(String sql);
	
	public abstract String replaceNext(String sql, String param);
	
	public abstract String replaceSpecific(String sql, String paramName, String param);
}
