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
package org.squashtest.ta.plugin.db.resources;


import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;

/**
 * SQL Query resource implementation.
 * @author fgaillard
 *
 */
@TAResource("query.sql")
public class SQLQuery implements Resource<SQLQuery> {
	

	private String queryString;
	
	public SQLQuery(){}
	
	public SQLQuery(String query){
//		List<String> instructions = splitInstructions(query);
//		if (instructions.size() > 1){
//			throw new BadDataException("");
//		}
		queryString = query;
	}
	
	@Override
	public SQLQuery copy() {
		String copiedQuery = "".concat(queryString);
		return new SQLQuery(copiedQuery);
	}

	@Override
	public void cleanUp() {
		
	}
	
	public String getQuery() {
		return queryString;
	}

	
}
