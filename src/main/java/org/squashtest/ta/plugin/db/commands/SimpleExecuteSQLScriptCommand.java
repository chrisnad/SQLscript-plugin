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
package org.squashtest.ta.plugin.db.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.squashtest.ta.core.tools.OptionsReader;
import org.squashtest.ta.framework.annotations.TACommand;
import org.squashtest.ta.framework.components.Command;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.plugin.db.resources.SQLResultSet;
import org.squashtest.ta.plugin.db.resources.SQLScript;
import org.squashtest.ta.plugin.db.targets.DatabaseTarget;

/**
 * <p>
 * Will take a sql script and execute it against the given database
 * </p>
 * 
 * @author bsiri
 * 
 */
@TACommand("execute")
public class SimpleExecuteSQLScriptCommand implements Command<SQLScript, DatabaseTarget> {

	private DatabaseTarget database;
	private SQLScript query;
	private Collection<Resource<?>> configuration = new LinkedList<Resource<?>>();
	private final static String KEEP_SEPARATOR = "keep.separator";
	private boolean keep = true;

	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		this.configuration.addAll(configuration);
	}

	@Override
	public void setTarget(DatabaseTarget target) {
		this.database = target;
	}

	@Override
	public void setResource(SQLScript resource) {
		query = resource;
	}

	@Override
	public SQLResultSet apply() {
		getOptions();
		List<String> queryToExecute = query.getBatch();
		if(!keep){
			queryToExecute = removeSeparator(queryToExecute);
		}
		database.execute(queryToExecute);
		// TODO : check the result
		// int[] result = database.execute(queryToExecute);
		return null;

	}

	private List<String> removeSeparator(List<String> queryToProcess) {
		List<String> processedList = new ArrayList<String>(queryToProcess.size());
		for (String aquery : queryToProcess) {
			aquery = aquery.trim();
			if(aquery.endsWith(";")){
				processedList.add(aquery.substring(0,aquery.length()-1));
			}else{
                            processedList.add(aquery);
                        }
		}
		return processedList;
	}

	protected void getOptions() {
		Map<String, String> options = null;
		for (Resource<?> resource : configuration) {
			if (FileResource.class.isAssignableFrom(resource.getClass())) {
				File fileR = ((FileResource) resource).getFile();
				options = readConf(fileR);
			}
		}
		if (options!= null && options.containsKey(KEEP_SEPARATOR)) {
			String value = options.get(KEEP_SEPARATOR);
			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				keep = Boolean.parseBoolean(value);
			} else {
				throw new BadDataException("Execute SQL script: \"" + value
						+ "\" is not a valid value for \"keep.saparator\" parameter. It can only be \"true\" or \"false\"");
			}
		}
	}

	/**
	 * @param file
	 *            : the configuration file to read
	 * @return a Map of the file type and path
	 */
	protected Map<String, String> readConf(File file) {
		try {
			return OptionsReader.BASIC_READER.getOptions(file);
		} catch (IOException ex) {
			throw new BadDataException("Execute SQL script command: an error occurred while reading the configuration : "+ex.getMessage(), ex);
		} catch (IllegalArgumentException ex) {
			throw new BadDataException("Execute SQL script command: an error occurred while reading the configuration : "+ex.getMessage(), ex);
		}
	}

	@Override
	public void cleanUp() {
		// nothing
	}

}
