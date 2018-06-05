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

import java.util.Properties;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.filter.IColumnFilter;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;

/**
 * Accepts a column if it is NOT a primary key.
 * @author edegenetais
 *
 */
public class NoPKFilter implements IColumnFilter {

	/** The dataset this filter is designed for */
	private IDataSet targetDataset;
	
	private PPKFilter delegatePPKFilter;
	
	public NoPKFilter(IDataSet dataSet) {
		if(dataSet==null){
			throw new IllegalArgumentException("dataset cannot be null");
		}
		this.targetDataset=dataSet;
	}
	
	public NoPKFilter(IDataSet dataSet,Properties pseudoPkDefinitions) {
		this(dataSet);
		if(pseudoPkDefinitions!=null){
			delegatePPKFilter=new PPKFilter(pseudoPkDefinitions);
		}
	}
	
	@Override
	public boolean accept(String tableName, Column column) {
		try {
			boolean accepted=acceptIfNotGenuinePK(tableName, column);
			
	
			if (accepted && delegatePPKFilter != null
					&& delegatePPKFilter.accept(tableName, column)) {
				accepted = false;
			}
			
			return accepted;
		} catch (DataSetException e) {
			throw new InstructionRuntimeException("Failed to get Table data for "+tableName, e);
		}
	}

	/**
	 * check if it is a real PK
	 * 
	 * @param tableName
	 *            table name ;)
	 * @param column
	 *            column definition data
	 * @return <code>true</code> if the key is not a genuine PK in the dataset,
	 *         <code>false</code> if it is a PK.
	 * @throws DataSetException
	 *             in case the PK metadata lookup fails.
	 */
	private boolean acceptIfNotGenuinePK(String tableName, Column column) throws DataSetException {
		boolean accepted=true;
		ITable table=targetDataset.getTable(tableName);
		ITableMetaData metadata=table.getTableMetaData();
		for(Column pk:metadata.getPrimaryKeys()){
			if(pk.getColumnName().equals(column.getColumnName())){
				accepted=false;
				break;
			}
		}
		return accepted;
	}

}
