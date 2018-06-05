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

import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.ColumnFilterTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.filter.IColumnFilter;

/**
 * Ported from CATS.
 * Decorates the tables of a dataset and exposes only some columns from them.
 * 
 * @author gf
 * @author edegenetais
 * 
 */
public class FilteredColumnDataSet extends AbstractDataSet implements IDataSet {
	private final IColumnFilter columnFilter;
	private final IDataSet filteredDataSet;

	/**
	 * Creates a {@link FilteredColumnDataSetTest} that decorates the specified dataset and exposes only the columns
	 * allowed by the specified filter.
	 * 
	 * @param filteredDataSet
	 *            the filtered dataset
	 * @param columnFilter
	 *            the column filtering strategy
	 * @param hidePK wether to hide primary keys or not.
	 * @param pseudoPrimaryKeys primary keys definitions, if any.
	 * @throws IllegalArgumentException
	 *             when parameter is null
	 */
	public FilteredColumnDataSet(IDataSet filteredDataSet, IColumnFilter columnFilter, boolean hidePK, Properties pseudoPrimaryKeys)
			throws IllegalArgumentException {

		if (columnFilter == null) {
			throw new IllegalArgumentException("columnFilter null");
		}
		if (filteredDataSet == null) {
			throw new IllegalArgumentException("filteredDataSet null");
		}

		if(hidePK){
			NoPKFilter pkFilter=new NoPKFilter(filteredDataSet, pseudoPrimaryKeys);
			CompositeColumnFilter compositeFilter=new CompositeColumnFilter();
			compositeFilter.addFilters(columnFilter,pkFilter);
			this.columnFilter=compositeFilter;
		}else{
			this.columnFilter = columnFilter;
		}
		this.filteredDataSet = filteredDataSet;
	}

	/**
	 * Simpler constructor for the case where we do not filter primary keys.
	 * @param filteredDataSet the filtered dataset
	 * @param columnFilter the column filtering strategy
	 * @throws IllegalArgumentException when parameter is null
	 */
	public FilteredColumnDataSet(IDataSet filteredDataSet,
			IColumnFilter columnFilter) throws IllegalArgumentException {
		this(filteredDataSet,columnFilter,false,null);
	}
	
	@Override
	protected ITableIterator createIterator(final boolean reversed) throws DataSetException {
		return new ITableIterator() {
			private final ITableIterator filteredTableIterator = createFilteredIterator(reversed);

			public ITable getTable() throws DataSetException {
				return new ColumnFilterTable(filteredTableIterator.getTable(), columnFilter);
			}

			public ITableMetaData getTableMetaData() throws DataSetException {
				return getTable().getTableMetaData();
			}

			public boolean next() throws DataSetException {
				return filteredTableIterator.next();
			}
		};
	}

	private ITableIterator createFilteredIterator(boolean reversed) throws DataSetException {
		return reversed ? filteredDataSet.reverseIterator() : filteredDataSet.iterator();
	}

}
