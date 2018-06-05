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
package org.squashtest.ta.plugin.db.library.dbunit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;

/**
 * Column filter to define column filtering table-wise. Tables are recognized by regex.
 * @author edegenetais
 *
 */
public class ByTableIncludeExcludeColumnFilter implements IColumnFilter {
	private Map<String,Pattern> patternMap=new HashMap<String, Pattern>();
	private Map<String,DefaultColumnFilter> filterMap=new HashMap<String, DefaultColumnFilter>();
	@Override
	public boolean accept(String tableName, Column column) {
		boolean accept=true;
		String previousMatch=null;
		for(Entry<String, Pattern> patternEntry:patternMap.entrySet()){
			//if we find some table regex matching the table name, let's go
			Matcher matcher=patternEntry.getValue().matcher(tableName);
			if(matcher.matches()){
				if(previousMatch!=null){
					throw new InstructionRuntimeException(
							"Column filtering conflict: table include regex "
									+ previousMatch
									+ " and "
									+ patternEntry.getKey()
									+ " both match "
									+ tableName
									+ " and have column filtering configuration.");
				}
				accept=filterMap.get(patternEntry.getKey()).accept(tableName, column);
				previousMatch=patternEntry.getKey();
			}
		}
		return accept;
	}
	
	public void addColumnIncludeFilter(String tableRegex, String columnRegex){
		DefaultColumnFilter filter = getOrCreateFilter(tableRegex);
		filter.includeColumn(columnRegex);
	}
	
	public void addColumnExcludeFilter(String tableRegex, String columnRegex){
		DefaultColumnFilter filter=getOrCreateFilter(tableRegex);
		filter.excludeColumn(columnRegex);
	}
	
	private DefaultColumnFilter getOrCreateFilter(String tableRegex) {
		DefaultColumnFilter filter;
		if(filterMap.containsKey(tableRegex)){
			filter=filterMap.get(tableRegex);
		}else{
			filter=new DefaultColumnFilter();
			filterMap.put(tableRegex, filter);
			Pattern pattern=Pattern.compile(tableRegex);
			patternMap.put(tableRegex, pattern);
		}
		return filter;
	}
}
