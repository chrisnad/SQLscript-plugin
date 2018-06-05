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
import java.util.List;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;

/**
 * Column filter implementing the composite pattern. A column is accepted by the composite filter if and only if each
 * component filter accepts the column.
 * 
 * @author gf
 * 
 */
public class CompositeColumnFilter implements IColumnFilter {
	
	private List<IColumnFilter> filters = new ArrayList<IColumnFilter>();

	public CompositeColumnFilter() {
	}

	public CompositeColumnFilter(IColumnFilter... componentFilters) {
		addFilters(componentFilters);
	}

	public void addFilters(IColumnFilter... filtersToAdd) {
		for (IColumnFilter filter : filtersToAdd) {
			this.filters.add(filter);
		}
	}

	public boolean accept(String tableName, Column column) {
		boolean accepted = true;

		for (IColumnFilter filter : filters) {
			accepted &= filter.accept(tableName, column);

			// shortcut
			if (!accepted) {
				break;
			}
		}

		return accepted;
	}

	/**
	 * Might be costy, use this method scarcely.
	 */
	@Override
	public String toString() {
		StringBuilder buffer=new StringBuilder();
		buffer.append("compositeTableFilter{");
		for(IColumnFilter filter:filters){
			buffer.append(filter.toString()).append(",");
		}
		if(filters.size()>0){
			buffer.setLength(buffer.length()-1);
		}
		buffer.append("}");
		return buffer.toString();
	}

}
