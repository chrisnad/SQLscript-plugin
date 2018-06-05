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
import java.util.List;

import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.plugin.db.library.sql.SQLFormatUtils;

/**
 * Pretty much the same than sql.query, but is not limited to one statement.
 *
 */



@TAResource("script.sql")
public class SQLScript implements Resource<SQLScript> {
    	
    private List<String> batch = new ArrayList<String>();
	
	public SQLScript(){}
	
	public SQLScript(List<String> script){
		//List<String> instructions = SQLFormatUtils.splitInstructions(script);
                List<String> instructions = SQLFormatUtils.splitSQLScript(script);
		for (String instruction : instructions){
			this.batch.add(instruction);
		}
	}
	
	@Override
	public SQLScript copy() {
		return new SQLScript(getBatch());
	}
	

	@Override
	public void cleanUp() {
		
	}
	
	public List<String> getBatch(){
		List<String> newBatch = new ArrayList<String>();
		newBatch.addAll(batch);
		return newBatch;
	}
	
	public String getBatchAsString() {
		StringBuilder builder=  new StringBuilder();
		for (String instr : batch){
			builder.append(instr);
		}
		return builder.toString();
	}

	
}
