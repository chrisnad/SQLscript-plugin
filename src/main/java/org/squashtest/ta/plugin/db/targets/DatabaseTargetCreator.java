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
package org.squashtest.ta.plugin.db.targets;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.core.library.properties.PropertiesKeySet;
import org.squashtest.ta.core.templates.FileBasedCreator;
import org.squashtest.ta.core.tools.PropertiesBasedCreatorHelper;
import org.squashtest.ta.framework.annotations.TATargetCreator;
import org.squashtest.ta.framework.components.TargetCreator;
import org.squashtest.ta.framework.exception.BrokenTestException;
import org.squashtest.ta.plugin.commons.library.ShebangCheck;
import org.squashtest.ta.plugin.db.targets.DatabaseTarget.DatasourceLifecycleManager;

import com.mchange.v2.c3p0.DataSources;

@TATargetCreator("target.creator.database")
public class DatabaseTargetCreator extends FileBasedCreator implements TargetCreator<DatabaseTarget> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTargetCreator.class);
	
	private static final ShebangCheck SHEBANG_CHECK = new ShebangCheck("db");
	
	public static final String DATABASE_DRIVER_KEY = "squashtest.ta.database.driver";
	public static final String DATABASE_URL_KEY = "squashtest.ta.database.url";
	public static final String DATABASE_USERNAME_KEY = "squashtest.ta.database.username";
	public static final String DATABASE_PASSWORD_KEY = "squashtest.ta.database.password";
	
	
	//Note that the following pattern will ignore the 'user' and 'password' conf for C3P0 
	private static final String DATABASE_POOL_CONF_PREFIX = "squashtest.ta.database.pool.";
	public static final String DATABASE_POOL_CONF_PATTERN = "squashtest\\.ta\\.database\\.pool\\.((?!(user)|(password)).*)";
	

	
	
	private Collection<String> basicKeys = Arrays.asList(new String[]{DATABASE_DRIVER_KEY, DATABASE_URL_KEY, DATABASE_USERNAME_KEY, DATABASE_PASSWORD_KEY});
	
	private PropertiesBasedCreatorHelper helper = new PropertiesBasedCreatorHelper();

	private boolean hasCorrectShebang;
	
	public DatabaseTargetCreator(){
		helper.setKeys(DATABASE_DRIVER_KEY, DATABASE_URL_KEY, DATABASE_USERNAME_KEY, DATABASE_PASSWORD_KEY, DatabaseTarget.SQUASH_TA_DATABASE_SCHEMA_KEY);
		helper.setKeysRegExp(DATABASE_POOL_CONF_PATTERN);
	}
	
	@Override
	public boolean canInstantiate(URL propertiesFile) {
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Testing eligibility of "+propertiesFile+" as Database configuration URL.");
		}
		boolean isDBConfiguration = false;
		try {
			//if there is shebang #!db (or no shebang at all, for retrocompatibility)
			hasCorrectShebang = SHEBANG_CHECK.hasShebang(propertiesFile);
			boolean shebangOk = !SHEBANG_CHECK.hasAnyShebang(propertiesFile,DatabaseTargetCreator.class.getSimpleName()) || hasCorrectShebang;
			
			if (shebangOk && isDatabaseConfigurationType(propertiesFile)){
				isDBConfiguration = true;				
			}
		} catch (IOException ioe) {
			throw new BrokenTestException("Cannot access transmitted target definition URL",ioe);
		}	
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(propertiesFile+" is"+(isDBConfiguration?" ":" not ")+"eligible.");
		}
		return isDBConfiguration;
	}

	@Override
	public DatabaseTarget createTarget(URL propertiesFile) {

		try {
		File file = getFileOrFail(propertiesFile);
		Properties properties = helper.getEffectiveProperties(file);
		
		//get the properties for the DatasourceLifecycleManager that will manage the datasource under the hood
		Properties basicProperties = extractBasicProperties(properties);
		Properties poolingProperties = extractPoolingProperties(properties);
		
		DatasourceLifecycleManager manager = new LocallyCreatedDatasourceManager(basicProperties, poolingProperties);
		
		//now get anonymized properties for the front Target that will use it
		Properties anonymised = helper.anonymize(properties, 1, DATABASE_USERNAME_KEY, DATABASE_PASSWORD_KEY);
		
		//return the target
		return new DatabaseTarget(manager, anonymised);
		} catch (IOException e) {
			throw new BrokenTestException("Could not read target definition.", e);
		} catch (URISyntaxException e) {
			throw new BrokenTestException("Definition URL was no valid URI", e);
		}
	}
	
	private boolean checkKeys(File file) throws IOException{
		
		Properties properties = helper.getEffectiveProperties(file);
		PropertiesKeySet keySet = new PropertiesKeySet(properties);		
		
		boolean requiredKeysOK = keySet.containsAll(new String[]{DATABASE_DRIVER_KEY, DATABASE_URL_KEY});
		
		String connectionString = properties.getProperty(DATABASE_URL_KEY);
		boolean connProtocolOK = ((connectionString!=null) && connectionString.matches("^jdbc:.*"));
		
		if (hasCorrectShebang){
			checkPotentialErrors(requiredKeysOK, connProtocolOK, file.getPath());
		}	
		return (requiredKeysOK  && connProtocolOK);			
	}
	
	
	private void checkPotentialErrors(boolean requiredKeysOK, boolean connProtocolOK, String path) {
		if (!requiredKeysOK){
			LOGGER.error("DatabaseTargetCreator : cannot create target '"+path+"',"+
							" the supplied configuration should supply at least the following settings : '"+DATABASE_DRIVER_KEY+"' and '"+DATABASE_URL_KEY+"'");
		}else if (!connProtocolOK){
			LOGGER.error("DatabaseTargetCreator : cannot create target '"+path+"',"+
						" the connection string for setting '"+DATABASE_URL_KEY+"' do not use the protocol 'jdbc'");				
		}		
	}

	protected Properties extractBasicProperties(Properties effective){
		Properties basic = new Properties();
		for (String key : basicKeys){
			String value = effective.getProperty(key);
			if (value!=null){
				basic.setProperty(key, value);			
			}
		}
		return basic;
	}
	
	@SuppressWarnings("unchecked")
	protected Properties extractPoolingProperties(Properties effective){
		Properties pooling = new Properties();
		
		Enumeration<String> keys = (Enumeration<String>)effective.propertyNames();
		
		while(keys.hasMoreElements()){
			String key = keys.nextElement();
			if (! basicKeys.contains(key)){
				String strippedKey = stripKeyPrefix(key);
				pooling.setProperty(strippedKey, effective.getProperty(key));
			}
		}
		
		return pooling;		
	}
	
	private String stripKeyPrefix(String key){
		int index = DATABASE_POOL_CONF_PREFIX.length();
		return key.substring(index);
	}
	
	
	private boolean isDatabaseConfigurationType(URL propertiesFile){
		//technical check
		File file = getFileOrNull(propertiesFile);
		if (file==null){
			return false;
		}
		boolean keys;
		try {
			//functional check
			keys = checkKeys(file);
		} catch (IOException e) {
			LOGGER.warn("Could not read file "+file);
			keys = false;
		}
		return keys;
	}
	
	/* **********************************************************************************************************************
	 *  local implementation of DatabaseTarget.DatasourceLifecycleManager 
	 * ******************************************************************************************************************** */
	
	
	static class LocallyCreatedDatasourceManager implements DatasourceLifecycleManager{
		
		private static final String INIT_ERROR_MESSAGE = "database : failed to init the connection pool, see :";

		private static final String RELEASE_ERROR_MESSAGE = "database : an error occured while destroying the connection pool";

		private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceLifecycleManager.class);

		private DataSource datasource;
		private Properties basicProperties;
		private Properties poolProperties;
		
		LocallyCreatedDatasourceManager(Properties basicProperties, Properties poolProperties){
			this.basicProperties=basicProperties;
			this.poolProperties=poolProperties;
		}
		
		@Override
		public final void init() {
			try {

				loadDrivers();
				
				DataSource source = createBasicDataSource();
				
				source = makePoolWith(source);
				
				datasource=source;
				
			} catch (SQLException e) {
				LOGGER.error(INIT_ERROR_MESSAGE, e);
				throw new BrokenTestException(INIT_ERROR_MESSAGE, e);
			}
		}
	
		private DataSource createBasicDataSource() throws SQLException{
			
			String jdbcUrl = basicProperties.getProperty(DATABASE_URL_KEY);
			String username = basicProperties.getProperty(DATABASE_USERNAME_KEY);
			String password = basicProperties.getProperty(DATABASE_PASSWORD_KEY);
			password = (password==null) ? "" : password;
			
			if (username!=null){
				return DataSources.unpooledDataSource(jdbcUrl, username, password);
			}else{
				return DataSources.unpooledDataSource(jdbcUrl);
			}
			
		}
		private DataSource makePoolWith(DataSource unpooledDS) throws SQLException{
			return DataSources.pooledDataSource(unpooledDS, poolProperties);
		}
		
		
		private void loadDrivers(){
			
			String driverName=null;
			
			try{
				
				driverName = basicProperties.getProperty(DATABASE_DRIVER_KEY);
				Class.forName(driverName);
				
			}catch(ClassNotFoundException ex){
				String message = "Squash Test Automation (severe) : could not find database driver '"+driverName+"'. "+
								 "Please ensure that the settings'"+DATABASE_DRIVER_KEY+"' and the classpath are correctly configured.";
				LOGGER.error(message, ex);
				throw new BrokenTestException(message, ex);
			}
		}
		
		@Override
		public final DataSource getDatasource() {
			return datasource;
		}

		@Override
		public final void release() {
			try{
				DataSources.destroy(datasource);
				datasource=null;
			}catch(SQLException ex){
				LOGGER.error(RELEASE_ERROR_MESSAGE, ex);
				throw new BrokenTestException(RELEASE_ERROR_MESSAGE,ex);
			}
		}	
	}
}
