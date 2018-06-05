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
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.core.tools.ExceptionLogger;
import org.squashtest.ta.core.tools.OptionsReader;
import org.squashtest.ta.framework.annotations.TACommand;
import org.squashtest.ta.framework.components.Command;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.VoidResource;
import org.squashtest.ta.framework.exception.IllegalConfigurationException;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;
import org.squashtest.ta.plugin.db.library.dbunit.DatabaseOperationReader;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;
import org.squashtest.ta.plugin.db.targets.DatabaseTarget;

/**
 * <p><strong>description</strong> Deletes the content of tables specified in a {@link DbUnitDatasetResource} from a {@link DatabaseTarget}. 
 * This command returns no result.<p>
 * 
 * <p><strong>Configuration (optional) : 
 * 	<ul>
 * 		<li>{@link DbUnitConfiguration} : additional configuration for the DbUnit connection. </li>
 * 		<li>{@link DbUnitPPKFilter} : A Pseudo Primary Key filter that will override the one supplied in the dbconfiguration if any. </li>
 * 		<li>{@link FileResource} : a FileResource which entries are comma separated pairs of &lt;key:value&gt; (note that column ':' is the separator) (see below)</li>
 *  </ul>
 * </p>
 * 
 * <p><strong>available options</strong>
 * 	<ul>
 * 		<li>operation : 'DELETE' or 'DELETE_ALL'. Default is 'DELETE_ALL'</li>
 * 	</ul>
 * </p>
 * 
 * <p><strong>DSL example : </strong>EXECUTE delete WITH my.dataset ON my.db USING my.dbu.ppk, my.dbu.conf, $(operation : DELETE) AS no.result.</p>
 * 
 * 
 * @author FOG
 *
 */
@TACommand("delete")
public class DbUnitDeleteCommand extends AbstractDbUnitCommand implements Command<DbUnitDatasetResource, DatabaseTarget> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbUnitDeleteCommand.class);
	private static final ExceptionLogger RTE_LOGGER = new ExceptionLogger(LOGGER, InstructionRuntimeException.class); 
	private static final ExceptionLogger ICE_LOGGER = new ExceptionLogger(LOGGER, IllegalConfigurationException.class);
		
	
	private static final String OPERATION_KEY = "operation";
	
	private DatabaseOperation operation = DatabaseOperation.DELETE_ALL;
		
	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		putConfiguration(configuration);
	}

	@Override
	public VoidResource apply(){
		try{
			configure();
			
			IDatabaseConnection connection = buildDbUnitConnection(); 
			
			operation.execute(connection, getDataset());
			
			return new VoidResource();
		}catch(DatabaseUnitException ex){
			
			String message = "db unit delete : an error occurred from within the DbUnit framework:";
			throw RTE_LOGGER.errAndThrow(message, ex);	
			
		}catch(SQLException ex){
			
			String message = "db unit delete : an error from within the database:";
			throw RTE_LOGGER.errAndThrow(message, ex);	
			
		}
		
	}
	
	@Override
	protected void applySpecificConfiguration(Resource<?> confElement){
		if (FileResource.class.isAssignableFrom(confElement.getClass())){
			Map<String, String> parameters = readConf(((FileResource)confElement).getFile());
			String op = parameters.get(OPERATION_KEY);
			if (op!=null){
				operation = DatabaseOperationReader.readOperation(op);
				checkValidOperation();
			}
		}
	}
	
	private void checkValidOperation(){
		if ( (operation != DatabaseOperation.DELETE) && (operation != DatabaseOperation.DELETE_ALL)){
			
			String message = "db unit delete : Illegal operation configuration. Operation must be one of : 'DELETE', 'DELETE_ALL'";
			throw ICE_LOGGER.errAndThrow(message, null);
		}
	}

	
	private Map<String, String> readConf(File file){
		try{
			return OptionsReader.BASIC_READER.getOptions(file);
		}
		catch(IOException ex){
			String message = "dbunit delete : an error occured while reading the configuration : ";
			throw ICE_LOGGER.errAndThrow(message, ex);
		}
		catch(IllegalArgumentException ex){
			String message = "dbunit delete : an error occured while reading the configuration : ";
			throw ICE_LOGGER.errAndThrow(message, ex);
		}
	}
	
	@Override
	public void cleanUp() {
		//nothing to do
	}

	
	

}
