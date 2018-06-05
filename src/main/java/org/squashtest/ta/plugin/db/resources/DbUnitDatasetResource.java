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

import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.plugin.db.library.dbunit.CompositeColumnFilter;
import org.squashtest.ta.plugin.db.library.dbunit.CompositeTableFilter;
import org.squashtest.ta.plugin.db.library.dbunit.FilteredStructureDataSet;

@TAResource("dataset.dbunit")
public class DbUnitDatasetResource implements Resource<DbUnitDatasetResource> {

	private IDataSet dataset;
	private boolean hasMetadata=false;
	
	public IDataSet getDataset() {
		return dataset;
	}

	/**
	 * @return <code>true</code> if and only if the metadata where extracted from the dataset source and included in the dataset. 
	 * <code>false</code> otherwise.
	 */
	public boolean hasMetadata(){
		return hasMetadata;
	}

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public DbUnitDatasetResource() {}
	
	/**
	 * Create a dataset resource from a dbunit dataset.
	 * 
	 * @param dataSet
	 *            the dataset to reference.
	 * @param hasMetadata
	 *            indicator. Pass <code>true</code> if the dataset contains
	 *            metadata, and false if it does not or there is no clue if it
	 *            does.
	 */
	public DbUnitDatasetResource(IDataSet dataSet,boolean hasMetadata){
		this.dataset=dataSet;
		this.hasMetadata=hasMetadata;
	}
	
	public DbUnitDatasetResource(List<ITableFilter> tableFilters, List<IColumnFilter> filters, IDataSet unFilteredDataset,boolean hasMetadata){
		ITableFilter tableCompositeFilter=new CompositeTableFilter(tableFilters);
		IColumnFilter columnCompositeFilter=new CompositeColumnFilter(filters.toArray(new IColumnFilter[filters.size()]));
		FilteredStructureDataSet decoratedDataset=new FilteredStructureDataSet(unFilteredDataset, tableCompositeFilter, columnCompositeFilter);
		this.dataset=decoratedDataset;
		this.hasMetadata=hasMetadata;
	}
	
	@Override
	public DbUnitDatasetResource copy() {
		/*
		 * shallow copy here. Please note that this does not endanger tests data
		 * as the dataset is NOT the original object, and a readonly interface.
		 */
		return new DbUnitDatasetResource(this.dataset,this.hasMetadata);
	}

	@Override
	public void cleanUp() {
		//noop (GC will be enough)
	}
}
