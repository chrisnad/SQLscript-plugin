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

import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;

/**
 * Data bean to hold the result of parsing an XML dbunit filter file.
 * 
 * @author edegenetais
 * 
 */
public class FilterConfiguration {
	/** Table component of the filter configuration */
	private ITableFilter tableFilter;
	/** Column component of the filter configuration */
	private IColumnFilter columnFilter;
	/** If primary key exclusion is active or not. */
	private boolean removePK;
	
	/**
	 * Complete initialization filter.
	 * @param tableFilter the table filter component of the configuration.
	 * @param columnFilter the column filter component of the configuration.
	 * @param removePK if primary key filtering have to be removed
	 */
	public FilterConfiguration(ITableFilter tableFilter, IColumnFilter columnFilter, boolean removePK) {
		this.tableFilter=tableFilter;
		this.columnFilter=columnFilter;
		this.removePK=removePK;
	}
	
	public ITableFilter getTableFilter() {
		return tableFilter;
	}
	
	public IColumnFilter getColumnFilter() {
		return columnFilter;
	}
	
	public boolean hasRemovePK(){
		return removePK;
	}
}
