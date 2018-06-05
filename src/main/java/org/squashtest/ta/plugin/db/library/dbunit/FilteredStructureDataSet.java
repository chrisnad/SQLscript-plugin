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

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;

/**
 * Ported from CATS.
 * Decorates a dataset and exposes only some tables from it. Tables are also decorated to expose only some columns.
 * 
 * @author gf
 * @author edegenetais
 * 
 */
public class FilteredStructureDataSet implements IDataSet {
	private final IDataSet filteredDataSet;
	
	/**
	 * Creates a {@link FilteredStructureDataSetTest} that decorates the specified dataset and exposes only the tables and
	 * columns allowed by the specified filters.
	 * 
	 * @param decoratedDataSet
	 *            the filtered dataset
	 * @param tableFilter
	 *            the table filtering strategy
	 * @param columnFilter
	 *            the column filtering strategy
	 * @param hidePK wether to hide primary keys or not
	 * @param pseudoPrimaryKeys primary keys definitions, if any
	 */
	public FilteredStructureDataSet(IDataSet decoratedDataSet, ITableFilter tableFilter,final IColumnFilter columnFilter, boolean hidePK, Properties pseudoPrimaryKeys) {
		//TODO remove the parameter redefinitions
		IColumnFilter actualColumnFilter;
		if(hidePK){
			CompositeColumnFilter compositeFilter=new CompositeColumnFilter();
			NoPKFilter noPK=new NoPKFilter(decoratedDataSet,pseudoPrimaryKeys);
			compositeFilter.addFilters(noPK);
			if(columnFilter!=null){
				compositeFilter.addFilters(columnFilter);
			}
			actualColumnFilter=compositeFilter;
		}else if(columnFilter==null){
			actualColumnFilter=new YesColumnFilter();
		}else{
			actualColumnFilter=columnFilter;
		}
		
		filteredDataSet = filterColumns(filterTables(decoratedDataSet, tableFilter), actualColumnFilter);
		
	}

	/**
	 * Creates a {@link FilteredStructureDataSetTest} that decorates the specified dataset and exposes only the tables and
	 * columns allowed by the specified filters.
	 * 
	 * @param decoratedDataSet
	 *            the filtered dataset
	 * @param tableFilter
	 *            the table filtering strategy
	 * @param columnFilter
	 *            the column filtering strategy
	 */
	public FilteredStructureDataSet(IDataSet decoratedDataSet, ITableFilter tableFilter,final IColumnFilter columnFilter) {
		this(decoratedDataSet,tableFilter,columnFilter,false,null);
	}
	
	private static IDataSet filterTables(IDataSet dataSetToFilter, ITableFilter tableFilter) {
		if (tableFilter == null) {
			throw new IllegalArgumentException("tableFilter is null");
		}

		return new FilteredDataSet(tableFilter, dataSetToFilter);
	}
	
	private static IDataSet filterColumns(IDataSet dataSetToFilter, IColumnFilter columnFilter) {
		if (columnFilter == null) {
			throw new IllegalArgumentException("columnFilter is null");
		}

		return new FilteredColumnDataSet(dataSetToFilter, columnFilter);
	}

	public ITable getTable(String tableName) throws DataSetException {
		return filteredDataSet.getTable(tableName);
	}

	public ITableMetaData getTableMetaData(String tableName) throws DataSetException {
		return filteredDataSet.getTableMetaData(tableName);
	}

	public String[] getTableNames() throws DataSetException {
		return filteredDataSet.getTableNames();
	}

	@SuppressWarnings("deprecation")
	public ITable[] getTables() throws DataSetException {
		return filteredDataSet.getTables();
	}

	public boolean isCaseSensitiveTableNames() {
		return filteredDataSet.isCaseSensitiveTableNames();
	}

	public ITableIterator iterator() throws DataSetException {
		return filteredDataSet.iterator();
	}

	public ITableIterator reverseIterator() throws DataSetException {
		return filteredDataSet.reverseIterator();
	}

}

