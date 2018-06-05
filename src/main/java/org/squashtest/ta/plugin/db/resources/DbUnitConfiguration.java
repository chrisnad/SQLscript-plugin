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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConfig.ConfigProperty;
import org.dbunit.database.IDatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.squashtest.ta.core.tools.io.FileTree.FILE_TREE;
import org.squashtest.ta.core.tools.io.PropertiesLoader;
import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.plugin.db.library.dbunit.PPKFilter;

/**
 * The DBunit configuration
 * @author FOG
 *
 */
@TAResource("conf.dbunit")
public class DbUnitConfiguration implements Resource<DbUnitConfiguration> {

	private static final String DBUNIT_CONF_PREFIX = "squashtest.ta.dbunit";
	private static final String PPK_FILTER_PROPERTIES = "squashtest.ta.DbUnitFilter.ppk";
	private static final String ESCAPE_PATTERN_PROPERTY = "escapePattern";	
	
	private static final PropertiesLoader PROPERTIES_LOADER=new PropertiesLoader();
	private static final Logger LOGGER=LoggerFactory.getLogger(DbUnitConfiguration.class);
	
	private DatabaseConfig databaseConfig = new DatabaseConfig();
	private Properties originalProperties = new Properties();
	private File propertiesFile;
	
	/**
	 * Default constructor for Spring enumeration only.
	 */
	public DbUnitConfiguration() {}

	/**
	 * Is instantiated with a Properties file
	 * @param properties
	 */
	public DbUnitConfiguration(File properties) {
		try {
			propertiesFile = properties;
			PROPERTIES_LOADER.loadAndStrip(properties, originalProperties, DBUNIT_CONF_PREFIX);
			configPpk(null);
			configEscape(originalProperties.getProperty(ESCAPE_PATTERN_PROPERTY));

		} catch (DatabaseUnitException dbue) {
			throw new BadDataException("Bad DB Unit properties file",dbue);
		} catch (FileNotFoundException fnfe) {
			throw new BadDataException("PPK Properties file not found",fnfe);
		} catch (IOException ioe) {
			throw new BadDataException("Input/Output Exception ",ioe);
		}
		
		
	}

	/**
	 * Is Instanciated via A Properties resource and a reference Path declaring where the 
	 * properties file containing the properties resource is located
	 * @param properties 
	 * @param referencePath
	 */
	public DbUnitConfiguration(Properties properties, String referencePath) {
		try{	
			originalProperties = properties;				
			configPpk(referencePath);			
			configEscape(originalProperties.getProperty(ESCAPE_PATTERN_PROPERTY));
			
		} catch (DatabaseUnitException dbue) {
			throw new BadDataException("Bad DB Unit properties file",dbue);
		} catch (FileNotFoundException fnfe) {
			throw new BadDataException("PPK Properties file not found",fnfe);
		} catch (IOException ioe) {
			throw new BadDataException("Input/Output Exception ",ioe);
		}
	}
	
	public DbUnitConfiguration(DatabaseConfig databaseConfig){
		this.databaseConfig = databaseConfig;
	}
	
	private void configPpk(String referencePath) throws IOException, DatabaseUnitException, FileNotFoundException{
		Set<String> entries = originalProperties.stringPropertyNames();
		PPKFilter pseudoPrimaryKeyFilter = null;
		for (String propertyName : entries) {
			if (PPK_FILTER_PROPERTIES.equals(propertyName)){
				String path = originalProperties.getProperty(PPK_FILTER_PROPERTIES);
				File ppkFilterProperties = new File(path);
				if (!ppkFilterProperties.isAbsolute()){
					if (referencePath == null){
						ppkFilterProperties = findAbsoluteFile(propertiesFile, path);
					}else{
						ppkFilterProperties = findAbsoluteFile(referencePath, path);
					}
				}
				Properties ppkProps=PROPERTIES_LOADER.load(ppkFilterProperties);
				pseudoPrimaryKeyFilter = new PPKFilter(ppkProps);
			}
		}
		//We remove the personally added property if it is present 
		originalProperties.remove(PPK_FILTER_PROPERTIES);
		//We then set the properties to the databaseConfig
		databaseConfig.setPropertiesByString(originalProperties);
		//We add our own PPK Filter property if it is not null
		if (pseudoPrimaryKeyFilter != null){
			databaseConfig.setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, pseudoPrimaryKeyFilter);
		}
	}
	
	@Override
	public DbUnitConfiguration copy() {
		File copiedFile = null;
		try{
			copiedFile = FILE_TREE.createTempCopyDestination(propertiesFile);
			FileUtils.copyFile(propertiesFile, copiedFile);
		} catch(IOException ioe) {
			throw new BadDataException("Input/Output Exception ",ioe);
		}
		return new DbUnitConfiguration(copiedFile);
	}

	@Override
	public void cleanUp() {
		//noop
	}
	
	public DatabaseConfig getConfiguration(){
		DatabaseConfig dbcfg = new DatabaseConfig();
		//To duplicate our current databaseConfig, we set all its keys and values to the same state
		setDatabaseConfigProperties(dbcfg);
		return dbcfg;
	}
	
	/**
	 * Configure the databaseConnection
	 * Updates the databaseConfig of the databaseConnection with our current databaseConfig
	 * @param databaseConnection
	 */
	public void configure(IDatabaseConnection databaseConnection){
		//Since there is no setter for the config on the databaseConnection, 
		//we will all the possible entries for the databaseConfig class
		DatabaseConfig currentDatabaseConfig = databaseConnection.getConfig();
		setDatabaseConfigProperties(currentDatabaseConfig);
	}
	
	private void setDatabaseConfigProperties(DatabaseConfig dbcfg){
		//We check for all possible properties in databaseConfig
		ConfigProperty[] possibleProperties = DatabaseConfig.ALL_PROPERTIES;
		for (ConfigProperty configProperty : possibleProperties) {
			String property = configProperty.getProperty();
			Object value = databaseConfig.getProperty(property);
			if (value != null){
				LOGGER.debug("Setting property "+property+" to value '"+value+"'");
				dbcfg.setProperty(property, value);
			}
		}
		//We lastly check for our own property PPK_FILTER_PROPERTIES
		if (databaseConfig.getProperty(PPK_FILTER_PROPERTIES) != null){
			dbcfg.setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, databaseConfig.getProperty(PPK_FILTER_PROPERTIES));
		}
	}
	
	private File findAbsoluteFile (File baseFile, String relativePath) throws IOException{
		String originalPath = baseFile.getAbsolutePath();
		return findAbsoluteFile(originalPath, relativePath);
	}
	
	private File findAbsoluteFile (String originalPath, String relativePath) throws IOException{
		String[] pathDirectories;
		String separator = File.separator;
		if ("\\".equals(separator)){
			pathDirectories = originalPath.split("\\\\");
		} else {
			pathDirectories = originalPath.split(separator);
		}
		String newPath = "";
		for (int i=0; i<pathDirectories.length-1; ++i) {
			newPath = newPath.concat(pathDirectories[i] + separator);
		}
		
		newPath = newPath.concat(relativePath);
		return new File(newPath).getCanonicalFile();
	}
	
	//Example of escapePattern: `?` Can be useful to escape keyword (for example database column called "unique")
	private void configEscape(String escapePattern){
		if (escapePattern != null){
			databaseConfig.setProperty(DatabaseConfig.PROPERTY_ESCAPE_PATTERN, escapePattern );
		}
	}
}
