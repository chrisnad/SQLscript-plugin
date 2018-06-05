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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;
import org.dbunit.dataset.filter.IColumnFilter;

/**
 * PseudoPrimaryKey filter. Accepts a column if it is a primary Key.
 * 
 * @author fgaillard
 * 
 */
public class PPKFilter implements IColumnFilter {

	private Map<String, List<String>> primaryKeysDef = new HashMap<String, List<String>>();

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public PPKFilter() {
	}

	public PPKFilter(Properties properties) {
		for (Object key : properties.keySet()) {
			String propName = (String) key;
			String pkDef = properties.getProperty(propName);
			String[] pkColmunNames = pkDef.split(",");
			List<String> pkColnamesNormalized = new ArrayList<String>(
					pkColmunNames.length);
			for (String colName : pkColmunNames) {
				pkColnamesNormalized.add(colName.toUpperCase());
			}
			primaryKeysDef.put(propName, pkColnamesNormalized);
		}
	}

	public boolean accept(String pTableName, Column pColumn) {
		List<String> pseudoKey = Collections.emptyList();
		if (!primaryKeysDef.isEmpty()) {
			pseudoKey = primaryKeysDef.get(pTableName);
			if (null == pseudoKey) {
				throw new IllegalArgumentException("table " + pTableName
						+ " non reconnue.");
			}
		}
		return pseudoKey.contains(pColumn.getColumnName().toUpperCase());
	}

	public boolean hasPpk(String tableName) {
		return primaryKeysDef.containsKey(tableName);
	}

	public List<String> getPpk(String tableName) {
		return primaryKeysDef.get(tableName);
		
	}

	
	/**
	 * This method verify that the {@link PPKFilter} is valid for the table given in
	 * argument. It return a {@link ValidationResult} which contains the result of the validation</br>
	 * <ul> 
	 * <li>
	 * If no pseudo primary key (ppk) is defined for this table then the current {@link PPKFilter} is valid
	 * for the table given in argument. The matchingColumn and notFoundColumn list of the {@link ValidationResult} 
	 * will be empty.</br>
	 *  
	 * </li>
	 * <li>
	 * If a ppk is defined for the table there is two cases:
	 * <ul>
	 * <li>If all the column of the ppk exist in the table then the validation is OK and the matchingColumn 
	 * of the {@link ValidationResult} will contain the matching {@link Column}. The notFoundColumnList will be empty.
	 * </li>
	 * <li>
	 * If some column (at least one) of the ppk don't exist in the table then the validation is KO. The matchingColumn
	 *  list will contain the {@link Column} found, and notFoundColumn list will contains the name of the not found columns
	 * </li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param tableMetaData A DbUnit {@code ITableMetaData}
	 * @return {@code ValidationResult} which contains whole informations concerning the validation 
	 * @throws DataSetException DbUnit exception occurs during the process
	 */
	public PPKFilter.ValidationResult validPpkDefinition(ITableMetaData tableMetaData) throws DataSetException {
		
		List<String> notFoundColumn = new ArrayList<String>();
		Column[] col = tableMetaData.getColumns();
		String tableName = tableMetaData.getTableName();
		List<String> ppkColumnList = getPpk(tableName);
		List<Column> matchingColumn = new ArrayList<Column>(ppkColumnList.size());
		for (String ppkColumnName : ppkColumnList) {
			try{
			
				int index = tableMetaData.getColumnIndex(ppkColumnName); 
				matchingColumn.add(col[index]);
			}
			catch (NoSuchColumnException e) {
				notFoundColumn.add(ppkColumnName);
			}
		}
		return new ValidationResult(tableName, matchingColumn, notFoundColumn);
	}
	
	/**
	 * Internal class for validation result
	 */
	public class ValidationResult{
		
		private String tableName;
		private List<String> notFoundColumn;
		private List<Column> matchingColumn;
		
		ValidationResult( String tableName, List<Column> matchingColumn, List<String> notFoundColumn){
			this.tableName=tableName;
			this.notFoundColumn = notFoundColumn;
			this.matchingColumn = matchingColumn;
		}

		public boolean isValid() {
			boolean isValid = false;
			if(notFoundColumn.size() == 0){
				isValid = true;
			}
			return isValid;
		}
		public Column[] getNotFoundColumn() {
			return notFoundColumn.toArray(new Column[notFoundColumn.size()]);
		}
		public Column[] getMatchingColumn() {
			return matchingColumn.toArray(new Column[matchingColumn.size()]);
		}
		
		public String getTableName() {
			return tableName;
		}
		
		/**
		 * This method must be called when the column(s) chosen as pseudo primary key for a table cannot be found.
		 * It generated the correct error message
		 * @param table : the concerned table, for personnalised error message
		 * @return the error message
		 * @throws DataSetException
		 */
		public String getNotFoundColumnAsString(ITableMetaData table) throws DataSetException {
			Column[] cols = table.getColumns();

			
			StringBuilder builder = new StringBuilder("The column(s) chosen as pseudo primary key for table \'");
			builder.append(tableName);
			builder.append("\' cannot be found in the dataset:\n");
			
			builder.append("- Pseudo primary key column(s) not found: [");		
			for (Iterator<String> iterator = notFoundColumn.iterator(); iterator.hasNext();) {
				builder.append(iterator.next().toLowerCase());
				if ( iterator.hasNext() ) {
					builder.append(", ");
				}	
			}
			builder.append("].\n");
			
			builder.append("- Dataset actual column(s): [");	
			for (int i = 0; i < cols.length ; i++) {
				builder.append(cols[i].getColumnName().toLowerCase());
				if (i < cols.length - 1) {
					builder.append(", ");
				}	
			}
			builder.append("].\n");

			return builder.toString();
		}
		
	}
	
}
