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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.plugin.commons.resources.XMLResource;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;
import org.xml.sax.InputSource;

/**
 * XML To Dataset Converter
 * Converts a XMLFile entry into a DBUnit Dataset 
 * The conversion is mainly done by DBunit parsers and converters
 * 
 * @author fgaillard
 *
 */
@TAResourceConverter("dataset")
public class XmlToDataset implements ResourceConverter<XMLResource, DbUnitDatasetResource> {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlToDataset.class);

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public XmlToDataset(){}
	
	@Override
	public float rateRelevance(XMLResource input) {
		return 0.5f;
	}
	
	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		//noop
		if(configuration.size()>0){
			LOGGER.warn("Ignoring {} configuration elements. No configuration for this converter.", configuration.size());
		}
	}

	@Override
	public DbUnitDatasetResource convert(XMLResource resource) {
		DbUnitDatasetResource dbUnitDataResource = null;
		try
		{
			InputSource iSource = new InputSource(new FileInputStream(resource.getXMLFile()));
			FlatXmlProducer producer = new FlatXmlProducer(iSource);
			IDataSet dataSet = new FlatXmlDataSet(producer);
			
			ReplacementDataSet rDataSet;
			rDataSet = new ReplacementDataSet(dataSet);
			rDataSet.setSubstringDelimiters("${", "}");
			rDataSet.setStrictReplacement(false);
			rDataSet.addReplacementObject(new String("[NULL]"), null);
			rDataSet.addReplacementObject(new String("[null]"), null);
			
			//we set hasMetadata to false here because the xml dataset includes no metadata
		    dbUnitDataResource = new DbUnitDatasetResource(rDataSet,false);
		} catch (FileNotFoundException fnfe) {
			throw new BadDataException("file not found!!!!!\n", fnfe);
		} catch (DataSetException e) {
			throw new BadDataException("Cannot create dataset, something must be wrong with the underlying xml\n", e);
		}
		return dbUnitDataResource;
	}

	@Override
	public void cleanUp() {
		
	}
}
