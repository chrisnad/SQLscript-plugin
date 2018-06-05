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
package org.squashtest.ta.plugin.db.converter;

import java.util.Collection;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.plugin.commons.resources.DirectoryResource;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;

/**
 * DirectoryResource (Containing CSVResources) To Dataset Converter
 * Converts a XMLFile entry into a DBUnit Dataset 
 * The conversion is mainly done by DBunit parsers and converters
 * 
 * @author fgaillard
 *
 */
@TAResourceConverter("dataset")
public class CSVToDataset implements ResourceConverter<DirectoryResource, DbUnitDatasetResource> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSVToDataset.class);

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public CSVToDataset(){}
	
	@Override
	public float rateRelevance(DirectoryResource input) {
		return 0.5f;
	}
	
	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		//Apparently DBUnit CSV reading cannot be configured...
		if(configuration.size()>0){
			LOGGER.warn("Ignoring {} configuration elements. No configuration for this converter.", configuration.size());
		}
	}

	@Override
	public DbUnitDatasetResource convert(DirectoryResource resource) {
		DbUnitDatasetResource dbUnitDataResource = null;
		try{
			IDataSet dataSet = new CsvDataSet(resource.getDirectory());
			
			ReplacementDataSet rDataSet;
			rDataSet = new ReplacementDataSet(dataSet);
			rDataSet.setSubstringDelimiters("${", "}");
			rDataSet.setStrictReplacement(false);
			rDataSet.addReplacementObject(new String("[NULL]"), null);
			rDataSet.addReplacementObject(new String("[null]"), null);
			
			dbUnitDataResource = new DbUnitDatasetResource(rDataSet,false);
		} catch (DataSetException dse) {
			throw new BadDataException("file not found!!!!!\n", dse);
		} 
		return dbUnitDataResource;
	}

	@Override
	public void cleanUp() {
		
	}
}
