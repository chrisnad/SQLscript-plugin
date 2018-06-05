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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.plugin.db.library.sql.SQLParamUtil;

/**
 * SQL Query resource implementation.
 * @author fgaillard
 *
 */
@TAResource("parameter.indexed.sql")
public class SQLIndexedParameters implements Resource<SQLIndexedParameters> {
	
	private List<String> parameters;
	
	public SQLIndexedParameters(){}
	
	/**
	 * Constructor in case the SQL Parameters are ordered by an index
	 * @param queryParameters
	 */
	public SQLIndexedParameters(List<String> queryParameters){
		parameters = queryParameters;
	}
	
	/**
	 * Returns a copy of the current resource (in this case SQLIndexedParameters)
	 * It will return a List of the parameters
	 */
	@Override
	public SQLIndexedParameters copy() {
		List<String> copiedResources = new ArrayList<String>();
		for (String parameter : parameters) {
			copiedResources.add(parameter);
		}
		return new SQLIndexedParameters(copiedResources);
	}

	@Override
	public void cleanUp() {
		
	}
	
	/**
	 * returns the parameter from the given position
	 * @param position
	 * @return the parameter
	 */
	public String getParam(int position){
		return parameters.get(position);
	}
	
	/**
	 * returns a collection of the couples index/value (of the parameters)
	 * @return List<String> 
	 */
	public List<String> getValues(){
		return new LinkedList<String>(parameters);
	}
	
	/**
	 * will iterate over the param list and the query and replace parameters until one of them runs out of arguments first.
	 * 
	 * @param sql
	 * @return
	 */
	public String setParams(String sql){
		Iterator<String> iterator = parameters.iterator();
		SQLParamUtil util = SQLParamUtil.POSITIONAL;
		
		String nextParam;
		String resultSql = sql;
		while(util.findNextParamName(resultSql)!=null){
			if (iterator.hasNext()){
				nextParam = iterator.next();			
				resultSql = util.replaceNext(resultSql, nextParam);
			}else{
				break;
			}
		}
		
		return resultSql;
		
	}
}
