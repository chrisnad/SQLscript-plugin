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
import java.util.Iterator;
import java.util.List;

import org.dbunit.database.DatabaseTableIterator;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.filter.ITableFilter;

/**
 * Implements a series of {@link ITableFilter} that only lets through if all elementary filters accept them.
 * @author edegenetais
 *
 */
public class CompositeTableFilter implements ITableFilter{
	
	private List<ITableFilter> filterList=new ArrayList<ITableFilter>();
	
	public CompositeTableFilter(List<ITableFilter> elementaryFilters) {
		filterList.addAll(elementaryFilters);
	}
	
	@Override
	public boolean accept(String tableName) throws DataSetException {
		boolean ok=true;
		Iterator<ITableFilter> iterator=filterList.iterator();
		while(ok && iterator.hasNext()){
			ITableFilter filter=iterator.next();
			ok=filter.accept(tableName);
		}
		return ok;
	}

	@Override
	public String[] getTableNames(IDataSet dataSet) throws DataSetException {
		String[] tableNames=dataSet.getTableNames();
		ArrayList<String> filteredList=new ArrayList<String>();
		for(String name:tableNames){
			if(accept(name)){
				filteredList.add(name);
			}
		}
		return filteredList.toArray(new String[filteredList.size()]);
	}

	@Override
	public ITableIterator iterator(IDataSet dataSet, boolean reversed)
			throws DataSetException {
		String[] tableNames=getTableNames(dataSet);
		if(reversed){
			String[] newTable=new String[tableNames.length];
			for(int i=0;i<tableNames.length;i++){
				newTable[tableNames.length-i-1]=tableNames[i];
			}
			tableNames=newTable;
		}
		return new DatabaseTableIterator(tableNames, dataSet);
	}

}
