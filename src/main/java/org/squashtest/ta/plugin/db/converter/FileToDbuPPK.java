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
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.PropertiesResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.plugin.commons.converter.FileToProperties;
import org.squashtest.ta.plugin.db.library.dbunit.PPKFilter;
import org.squashtest.ta.plugin.db.resources.DbUnitPPKFilter;

/**
 * File Resource To DbuPPK Filter
 * Converts a File Resource entry containing a Properties File into a pseudo primary key dbunit filter
 * <p>Every line of the Properties File must have the following specifications :<p>
 * <ul>
 * 	<li>The table name</li>
 * 	<li>spaces or tabs</li>
 *  <li>=</li>
 *  <li>spaces or tabs</li>
 *  <li>the column name (or names separated by a comma)</li>
 * </ul>
 * 
 * @author fgaillard
 *
 */
@TAResourceConverter("structured")
public class FileToDbuPPK implements ResourceConverter<FileResource, DbUnitPPKFilter> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileToDbuPPK.class);

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public FileToDbuPPK(){}
	
	@Override
	public float rateRelevance(FileResource input) {
		return 0.5f;
	}
	
	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		//Noop
		if(configuration.size()>0){
			LOGGER.warn("Ignoring {} configuration elements. No configuration for this converter.", configuration.size());
		}
	}

	/**
	 * The converter will first convert the File Resource into a PropertiesResource
	 * by using the appropriate converter. This converter will test the coherence 
	 * of the properties file structure
	 * 
	 *  It then uses this new PropertiesResource to convert it into a DbUnitPPKFilter
	 */
	@Override
	public DbUnitPPKFilter convert(FileResource resource) {
		FileToProperties propertiesConverter = new FileToProperties();
		PropertiesResource propertiesResource = propertiesConverter.convert(resource);
		Properties ppkProperties = new Properties();
		Set<String> myEntries = propertiesResource.getProperties().stringPropertyNames();
		for (String myEntry : myEntries) {
			ppkProperties.setProperty(myEntry, propertiesResource.getProperties().getProperty(myEntry));
		}
		PPKFilter filter = new PPKFilter(ppkProperties);
		
		return new DbUnitPPKFilter(filter);
	}

	@Override
	public void cleanUp() {
		//noop
	}
	
}
