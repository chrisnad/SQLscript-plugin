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
import org.squashtest.ta.framework.components.PropertiesResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.plugin.db.library.dbunit.PPKFilter;
import org.squashtest.ta.plugin.db.resources.DbUnitPPKFilter;

/**
 * Properties Resource To DbuPPK Filter
 * Converts a Properties Resource entry into a pseudo primary key dbunit filter
 * <p>Every line of the properties file contained by the Properties Resource has the following specifications </br>
 * (It has been tested by the FileToProperties converter):<p>
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
@TAResourceConverter("from.properties")
public class PropertiesToDbuPPK implements ResourceConverter<PropertiesResource, DbUnitPPKFilter> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesToDbuPPK.class);

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public PropertiesToDbuPPK(){}
	
	@Override
	public float rateRelevance(PropertiesResource input) {
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
	public DbUnitPPKFilter convert(PropertiesResource resource) {
		Properties ppkProperties = new Properties();
		Set<String> myEntries = resource.getProperties().stringPropertyNames();
		for (String myEntry : myEntries) {
			ppkProperties.setProperty(myEntry, resource.getProperties().getProperty(myEntry));
		}
		PPKFilter filter = new PPKFilter(ppkProperties);
		
		return new DbUnitPPKFilter(filter);
	}

	@Override
	public void cleanUp() {
		//noop
	}
	
}
