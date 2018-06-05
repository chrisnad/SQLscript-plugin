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

import java.util.Properties;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.plugin.db.library.dbunit.FilteredColumnDataSet;
import org.squashtest.ta.plugin.db.library.dbunit.FilteredStructureDataSet;
import org.squashtest.ta.plugin.db.library.dbunit.YesColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.parser.FilterConfiguration;

/**
 * This resource implements filtering of a dataset filter in the two DbUnit
 * dimensions:
 * <ul>
 * <li>Table filtering</li>
 * <li>Column filtering</li>
 * </ul>
 * 
 * @author edegenetais
 * 
 */
@TAResource("filter.dbunit")
public class DbUnitFilterResource implements Resource<DbUnitFilterResource>{

	/** Table component of the filter. */
	private ITableFilter tableFilter;
	/** Column component of the filter. */
	private IColumnFilter columnFilter;
	
	/** Wether to hide PK or not */
	private boolean hidePK;
	
	/** Pseudo primary keys definition, if any. */
	private Properties pseudoPrimaryKeys;
	
	/** noarg constructor for Spring enumeration */
	public DbUnitFilterResource() {}
	
	/**
	 * Encapsulate a filter that only filters whole tables.
	 * @param filter the table filter to apply.
	 */
	public DbUnitFilterResource(ITableFilter filter){
		this.tableFilter=filter;
		this.columnFilter=new YesColumnFilter();
	}
	
	/**
	 * Encapsulate a filter that only filters tables.
	 * @param filter the column filter to apply.
	 * This configuration will not hide primary keys.
	 */
	public DbUnitFilterResource(IColumnFilter filter){
		this.columnFilter=filter;
	}
	
	/**
	 * Encapsulate a filter with whole filter configuration.
	 * @param configuration filter configuration to apply.
	 * 
	 */
	public DbUnitFilterResource(FilterConfiguration configuration, Properties pseudoPrimaryKeys){
		this.tableFilter=configuration.getTableFilter();
		this.columnFilter=configuration.getColumnFilter();
		this.hidePK=configuration.hasRemovePK();
		this.pseudoPrimaryKeys=pseudoPrimaryKeys;
	}
	
	/**
	 * @param tableFilter
	 * @param columnFilter
	 * @param hidePK
	 * @param pseudoPrimaryKeys
	 */
	public DbUnitFilterResource(ITableFilter tableFilter,
			IColumnFilter columnFilter, boolean hidePK,
			Properties pseudoPrimaryKeys) {
		this.tableFilter = tableFilter;
		this.columnFilter = columnFilter;
		this.hidePK = hidePK;
		this.pseudoPrimaryKeys = pseudoPrimaryKeys;
	}

	@Override
	public DbUnitFilterResource copy() {
		return new DbUnitFilterResource(tableFilter,columnFilter,hidePK,pseudoPrimaryKeys);
	}

	@Override
	public void cleanUp() {
		//noop (GC will be enough)
	}

	public IDataSet apply(IDataSet orignialDS){
		if(tableFilter==null){
			return new FilteredColumnDataSet(orignialDS, columnFilter, hidePK, pseudoPrimaryKeys);
		}else{
			return new FilteredStructureDataSet(orignialDS, tableFilter, columnFilter,hidePK, pseudoPrimaryKeys);
		}
	}
	
}
