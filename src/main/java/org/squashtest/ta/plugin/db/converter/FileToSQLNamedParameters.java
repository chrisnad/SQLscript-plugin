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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.core.tools.io.SimpleLinesData;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.plugin.db.resources.SQLNamedParameters;

/**
 * File To SQLIndexedParameter Converter
 * Converts a File entry into an interpretable SQL Syntax
 * <p> The file must have the following specifications :<p>
 * <ul>
 * 	<li>Every line of the file contains 2 values separated by a equals '=',</li>
 * 	<li>The 2 values being : name and value</li>
 *  <li>none of the values can be empty except :</li>
 *  <ul>
 *  <li>If the name contains only spaces, the name will be defined by the spaces characters</li>
 *  </ul>
 * </ul>
 * 
 * @author fgaillard
 *
 */
@TAResourceConverter("from.text")
public class FileToSQLNamedParameters implements ResourceConverter<FileResource, SQLNamedParameters> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileToSQLNamedParameters.class);

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public FileToSQLNamedParameters(){}
	
	@Override
	public float rateRelevance(FileResource input) {
		return 0.5f;
	}
	
	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		if(configuration.size()>0){
			LOGGER.warn("Ignoring {} configuration elements. No configuration for this converter.", configuration.size());
		}
	}

	@Override
	public SQLNamedParameters convert(FileResource resource) {
		SQLNamedParameters namedParameters = null;
		try
		{
			SimpleLinesData data=new SimpleLinesData(resource.getFile().getPath());
			Map<String, String> tempParamters = new HashMap<String, String>();
			for(String line:data.getLines()){
				/* "=" is the current behaviour (1.5)
				 * "," is the old behaviour (but we keep it for retrocompatibility)
				 */
				if (line.contains(",")||line.contains("=")){
					String[] parameters = line.contains("=") ? line.split("=") : line.split(",");
					tempParamters.put(parameters[0], parameters[1]);
				} else {
					throw new BadDataException("The fileResource is not correctly configured : there is no \",\" to separate the values from their position");
				}
			}
			namedParameters = new SQLNamedParameters(tempParamters);
		} catch (FileNotFoundException fnfe) {
			LOGGER.error("The file "+resource.getFile().getName()+" was not found.");
		} catch (IOException ioe) {
			LOGGER.error("I/O error when converting the file "+resource.getFile().getName());
		}
		return namedParameters;
	}

	@Override
	public void cleanUp() {
		//noop
	}
}
