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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Adapter to give easy access to database metadata.
 * 
 * @author edegenetais
 * 
 */
public class DatabaseMetadataExplorer {
	
	private static final int PRIMARY_KEY_PK_INDEX_RS_INDEX = 5;
	private static final int PRIMARY_KEY_COLUMN_NAME_RS_INDEX = 4;
	
	private DatabaseMetaData metadata;
	private String schemaName;

	private Map<String, List<String>> pkCache=new HashMap<String, List<String>>();
	
	/**
	 * Create a metadata explorer for a given set of metadata and a given schema (if available).
	 * @param metadata the JDBC metadata reference.
	 * @param schemaName the referenced schema name. May be null if no schema filtering is needed.
	 */
	public DatabaseMetadataExplorer(DatabaseMetaData metadata, String schemaName) {
		this.metadata = metadata;
		this.schemaName = schemaName;
	}

	/**
	 * Search the metadata for the list of primary keys of a given table.
	 * @param table name of the table to lookup.
	 * @return a list of string containing the columns of the primary key in their order.
	 * @throws SQLException in case of error during metadata extraction.
	 */
	public synchronized List<String> getPrimaryKeyNames(String table) throws SQLException {
		List<String> pkList = pkCache.get(table);
		if (pkList == null) {

			SortedMap<Integer, String> pkMap = new TreeMap<Integer, String>();
			ResultSet pkRs = metadata.getPrimaryKeys(null, schemaName, table);
			while (pkRs.next()) {
				String columnName = pkRs
						.getString(PRIMARY_KEY_COLUMN_NAME_RS_INDEX);
				Integer pkIndex = pkRs.getInt(PRIMARY_KEY_PK_INDEX_RS_INDEX);
				if (pkRs.wasNull()) {// some DBMS drivers (eg: SQlite) don't
										// give the pk index...
					pkMap.put(pkRs.getRow(), columnName);
				} else {
					pkMap.put(pkIndex, columnName);
				}
			}
			pkRs.close();
			pkList = new ArrayList<String>(pkMap.values());
		}
		return pkList;
	}
	
	public void dispose() throws SQLException{
		metadata.getConnection().close();
	}
}
