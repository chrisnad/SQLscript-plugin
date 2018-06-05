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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.plugin.db.library.sql.QueryParameter;
import org.squashtest.ta.plugin.db.library.sql.SQLParamUtil;

/**
 * SQL Query resource implementation.
 * @author fgaillard
 *
 */
@TAResource("parameter.named.sql")
public class SQLNamedParameters implements Resource<SQLNamedParameters> {

	private Map<String, String> parameters;
	
	public SQLNamedParameters(){}
	
	/**
	 * Constructor in case the SQL Parameters are ordered by a name 
	 * @param queryParameters
	 */
	public SQLNamedParameters(Map<String, String> queryParameters){
		parameters = queryParameters;
	}
	
	/**
	 * Returns a copy of the current resource (in this case SQLNamedParameters)
	 * It will return a Map of the parameters indexed by there name
	 */
	@Override
	public SQLNamedParameters copy() {
		Map<String, String> copiedResources = new HashMap<String, String>();
		Set<String> myKeys = parameters.keySet();
		for (String key : myKeys) {
			copiedResources.put(key, parameters.get(key));
		}
		return new SQLNamedParameters(copiedResources);
	}

	@Override
	public void cleanUp() {
		
	}
	
	/**
	 * returns the parameter of the given name
	 * @param name
	 * @return the parameter
	 */
	public String getParam(String name){
		return parameters.get(name);
	}
	
	/**
	 * returns a collection of QueryParameter objects which contain the names and values of the 
	 * query paramters
	 * @return List<QueryParameter>
	 */
	public List<QueryParameter> getValues(){
		List<QueryParameter> indexValuesCouples = new ArrayList<QueryParameter>(); 
		Set<String> myKeys = parameters.keySet();
		for (String key : myKeys) {
			QueryParameter queryParameter = new QueryParameter(key, parameters.get(key));
			indexValuesCouples.add(queryParameter);
		}
		return indexValuesCouples;
	}
	
	
	/**
	 * Applies all possible parameters.
	 * 
	 * @param sql
	 * @return
	 */
	public String setParams(String sql){
		String result = sql;
		SQLParamUtil util = SQLParamUtil.NAMED;
		
		for (Entry<String, String> entry : parameters.entrySet()){
			result = util.replaceSpecific(result, entry.getKey(), entry.getValue());
		}
		
		return result;
		
	}
}
