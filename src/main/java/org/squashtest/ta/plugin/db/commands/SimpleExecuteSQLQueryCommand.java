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
package org.squashtest.ta.plugin.db.commands;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.squashtest.ta.framework.annotations.TACommand;
import org.squashtest.ta.framework.components.Command;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.plugin.db.resources.SQLIndexedParameters;
import org.squashtest.ta.plugin.db.resources.SQLNamedParameters;
import org.squashtest.ta.plugin.db.resources.SQLQuery;
import org.squashtest.ta.plugin.db.resources.SQLResultSet;
import org.squashtest.ta.plugin.db.targets.DatabaseTarget;



/**
 * <p>Will take a sql query, apply the configuration if any, then call the database and restitute the result set.</p>
 * 
 * <p>Accepts as configuration : 
 * 	<ul>
 * 		<li>{@link SQLIndexedParameters}</li>
 * 		<li>{@link SQLNamedParameters}</li>
 * 	</ul>
 * </p>
 * @author bsiri
 *
 */
@TACommand("execute")
public class SimpleExecuteSQLQueryCommand implements Command<SQLQuery, DatabaseTarget> {

	
	private List<Resource<?>> configuration = new ArrayList<Resource<?>>();
	private DatabaseTarget database;
	private SQLQuery query;
	
	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		this.configuration.addAll(configuration);
	}

	@Override
	public void setTarget(DatabaseTarget target) {
		this.database = target;
	}

	@Override
	public void setResource(SQLQuery resource) {
		query=resource;
	}

	@Override
	public SQLResultSet apply() {
		
		applyParameters();		
		
		ResultSet result = database.execute(query.getQuery());
		
		
		if (result==null){
			return null;
		}else{		
			return new SQLResultSet(result);
		}
		
	}
	
	
	protected void applyParameters(){
		
		if (configuration.size()==0){
			return;
		}
		
		String query = this.query.getQuery();
		
		for (Resource<?> resource : configuration){
			if (SQLIndexedParameters.class.isAssignableFrom(resource.getClass())){
				query = ((SQLIndexedParameters)resource).setParams(query);
			}else if (SQLNamedParameters.class.isAssignableFrom(resource.getClass())){
				query = ((SQLNamedParameters)resource).setParams(query);
			}else{
				throw new IllegalArgumentException("execute sql : the supplied configuration is not related to sql query parameterization");
			}
		}
		
		this.query = new SQLQuery(query);
		
	}

		
	@Override
	public void cleanUp() {
		//nothing
	}


	
}
