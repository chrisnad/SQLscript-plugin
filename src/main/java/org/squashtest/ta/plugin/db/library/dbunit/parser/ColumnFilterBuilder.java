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
package org.squashtest.ta.plugin.db.library.dbunit.parser;

import java.util.Collection;

import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.ByTableIncludeExcludeColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.CompositeColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.NoTimestampColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.YesColumnFilter;

class ColumnFilterBuilder {

	private IColumnFilter filter;

	private CompositeColumnFilter composite;

	private ByTableIncludeExcludeColumnFilter byTableFilter;

	private DefaultColumnFilter globalFilter;
	
	/**
	 * Add a filter to exclude timestamp columns from filtered datasets.
	 */
	public void addTimestampExcludeFilter(Collection<String> typeNames) {
		if(typeNames.isEmpty()){
			typeNames.add("timeStamp");
		}
		NoTimestampColumnFilter newFilter = new NoTimestampColumnFilter(typeNames);
		addFilter(newFilter);
	}

	private void addFilter(IColumnFilter newFilter) {
		if (filter == null) {
			filter = newFilter;
		} else {
			if (composite == null) {
				composite = new CompositeColumnFilter();
				composite.addFilters(filter);
				filter = composite;
			}
			composite.addFilters(newFilter);
		}
	}

	public void includeColumn(String tableRegex, String columnRegex) {
		if (tableRegex == null) {
			ensureGlobalFilter();
			globalFilter.includeColumn(columnRegex);
		} else {
			if(byTableFilter==null){
				ByTableIncludeExcludeColumnFilter newFilter=new ByTableIncludeExcludeColumnFilter();
				byTableFilter=newFilter;
				addFilter(newFilter);
			}
			byTableFilter.addColumnIncludeFilter(tableRegex, columnRegex);
		}
	}

	private void ensureGlobalFilter() {
		if(globalFilter==null){
			DefaultColumnFilter global=new DefaultColumnFilter();
			globalFilter=global;
			addFilter(global);
		}
	}

	public void excludeColumn(String tableRegex, String columnRegex) {
		if (tableRegex == null) {
			ensureGlobalFilter();
			globalFilter.excludeColumn(columnRegex);
		} else {
			if(byTableFilter==null){
				ByTableIncludeExcludeColumnFilter newFilter=new ByTableIncludeExcludeColumnFilter();
				byTableFilter=newFilter;
				addFilter(newFilter);
			}
			byTableFilter.addColumnExcludeFilter(tableRegex, columnRegex);
		}
	}

	public IColumnFilter getColumnFilter() {
		if (filter == null) {
			return new YesColumnFilter();
		}
		return filter;
	}

}
