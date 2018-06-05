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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.plugin.commons.converter.FileToProperties;
import org.squashtest.ta.plugin.db.resources.DbUnitConfiguration;

/**
 * File Resource To Dbu Configuration
 * Converts a File Resource entry containing a Properties File into a DbUnit Configuration entity
 * <p>Every line of the properties file contained by the Properties Resource has the following specifications :<p>
 * <ul>
 * 	<li>The property name</li>
 * 	<li>spaces or tabs</li>
 *  <li>=</li>
 *  <li>spaces or tabs</li>
 *  <li>The property value</li>
 * </ul>
 * 
 * @author fgaillard
 *
 */
@TAResourceConverter("structured")
public class FileToDbuConfig implements ResourceConverter<FileResource, DbUnitConfiguration> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileToDbuConfig.class);

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public FileToDbuConfig(){}
	
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

	@Override
	public DbUnitConfiguration convert(FileResource resource) {
		//First we test if the properties File contained in the file resource is correctly written
		//We use for that the FileToProperties converter
		FileToProperties converter = new FileToProperties();
		//converting a badly written properties File will throw a BadDataException
		converter.convert(resource);
		//If no exception was thrown, the file is valid, we can now create the DbUConfig Resource
		
		return new DbUnitConfiguration(resource.getFile());
	}

	@Override
	public void cleanUp() {
		//noop
	}
}
