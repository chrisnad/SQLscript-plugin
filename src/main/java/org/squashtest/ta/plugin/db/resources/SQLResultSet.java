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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;

/**
 * SQL Query resource implementation.
 * @author fgaillard
 *
 */
@TAResource("result.sql")
public class SQLResultSet implements Resource<SQLResultSet> {

	private ResultSet queryResultSet;
	
	public SQLResultSet(){}
	
	public SQLResultSet(ResultSet resultSet){
		queryResultSet = resultSet;
	}
	
	@Override
	public SQLResultSet copy() {
		return new SQLResultSet(queryResultSet);
	}

	@Override
	public void cleanUp() {
		try {
			queryResultSet.close();
		} catch (SQLException sqle) {
			throw new InstructionRuntimeException("The closing of the result set caused the following exception : ",sqle);
		}
	}
	
	public List<String>getColumnNames(){
		try {
			ResultSetMetaData metaData = queryResultSet.getMetaData();
			int nbColumns = metaData.getColumnCount();
			List<String>columnNames = new ArrayList<String>(nbColumns);
			for (int i = 1; i < nbColumns+1; i++) {
				columnNames.add(metaData.getColumnName(i));
			}
			return columnNames;
		} catch (SQLException sqle) {
			throw new InstructionRuntimeException("The retrieval of the query columns the following exception : ",sqle);
		}
	}
	
	public ResultSet getResultSet(){
		return queryResultSet;
	}
}
