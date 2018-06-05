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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.plugin.db.library.dbunit.PPKFilter;
import org.squashtest.ta.plugin.db.library.sql.DatabaseMetadataExplorer;
import org.squashtest.ta.plugin.db.resources.DbUnitConfiguration;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;
import org.squashtest.ta.plugin.db.resources.DbUnitPPKFilter;
import org.squashtest.ta.plugin.db.targets.DatabaseTarget;

public abstract class AbstractDbUnitCommand {

	private static final Column[] EMPTY_PK_TABLE = new Column[]{};
	private DatabaseTarget database;
	private DbUnitDatasetResource dataset;
	private DbUnitConfiguration config;
	private PPKFilter filter;
	
	private Collection<Resource<?>> configuration = new ArrayList<Resource<?>>();

	public AbstractDbUnitCommand() {
		super();
	}

	public void setResource(DbUnitDatasetResource resource){
		this.dataset=resource;
	}
	
	protected IDataSet getDataset(){
		return dataset.getDataset();
	}
	
	public void setTarget(DatabaseTarget target) {
		database=target;
	}

	protected void putConfiguration(Collection<Resource<?>> confResources){
		configuration.addAll(confResources);
	}
	
	/**
	 * Configure the dbunit {@link IDatabaseConnection}.
	 * @return the dbunit connection, configured from available configuration information.
	 * @throws DatabaseUnitException
	 * @throws SQLException 
	 */
	protected IDatabaseConnection buildDbUnitConnection()
			throws DatabaseUnitException, SQLException {
		
				Connection conn = database.getConnection();
		
				String schemaName = database.getConfiguration().getProperty(DatabaseTarget.SQUASH_TA_DATABASE_SCHEMA_KEY);
				
				IDatabaseConnection connection = new DatabaseConnection(conn,schemaName);
				
				if (config!=null){
					config.configure(connection);
				}
				
				if (filter!=null){
					DatabaseConfig connConfiguration = connection.getConfig();
					PPKFilter finalFilter=createPPKDefinitions();
					connConfiguration.setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, finalFilter);
				}
				
				return connection;
	}
	
	/**
	 * The only acceptable FileResource will be to configure the DbOperation. 
	 */
	protected void configure(){
		for (Resource<?> resource : configuration){
			Class<?> resClass = resource.getClass();
			
			if (DbUnitPPKFilter.class.isAssignableFrom(resClass)){
				filter = ((DbUnitPPKFilter) resource).getFilter();
			}
			else if ( DbUnitConfiguration.class.isAssignableFrom(resClass)){
			 	config = (DbUnitConfiguration) resource;
			}else{
				applySpecificConfiguration(resource);
			}
			
		}
	}
	
	protected abstract void applySpecificConfiguration(Resource<?> element);
	
	private PPKFilter createPPKDefinitions()
			throws DataSetException, SQLException {
		PPKFilter completeFilter;
		if (dataset != null && dataset.hasMetadata()) {
			completeFilter = createCompleteFilterFromDataset();
		}else{
			completeFilter = createCompleteFilterFromMetadata();
		}
		return completeFilter;
	}

	private PPKFilter createCompleteFilterFromMetadata() throws DataSetException, SQLException {
		StringBuilder columnList=new StringBuilder();

		Properties pkProps=new Properties();
		for(String tableName:dataset.getDataset().getTableNames()){
			if(filter.hasPpk(tableName)){
				pkProps = extractMetadataFromPpk(pkProps, tableName);
			}else{
				DatabaseMetadataExplorer metadataExplorer=database.getMetadataExplorer();
				List<String> pkList=metadataExplorer.getPrimaryKeyNames(tableName);
				for(String pkName:pkList){
					columnList.append(pkName).append(",");
				}
				//we delete last comma before affecting it.
				String pk = columnList.substring(0, columnList.length()-1);
				pkProps.setProperty(tableName, pk);
			}
		}
		return new PPKFilter(pkProps);
	}

	private PPKFilter createCompleteFilterFromDataset() throws DataSetException {
		StringBuilder columnList=new StringBuilder();
		PPKFilter completeFilter;
		Properties ppkDefinitions=new Properties();
		for (String tableName : dataset.getDataset().getTableNames()) {
			if (filter.hasPpk(tableName)) {
				ppkDefinitions = extractMetadataFromPpk(ppkDefinitions,	tableName);
			}else{
				ITableMetaData tableMetadata = dataset.getDataset().getTableMetaData(tableName);
				Column[] primaryKeys = tableMetadata.getPrimaryKeys();
				if(primaryKeys==null){
					primaryKeys=EMPTY_PK_TABLE;
				}
				createPrimaryKeyList(ppkDefinitions, columnList, tableName,primaryKeys);
			}
		}
		completeFilter=new PPKFilter(ppkDefinitions);
		return completeFilter;
	}

	private Properties extractMetadataFromPpk(Properties ppkDefinitions,
			String tableName)
			throws DataSetException {
		StringBuilder tableNoPpk = new StringBuilder("");
		ITableMetaData tableMetadata=dataset.getDataset().getTableMetaData(tableName);
		for(Column c:tableMetadata.getColumns()){
			if(filter.accept(tableName, c)){
				tableNoPpk.append(c.getColumnName()).append(",");
			}
		}
		//we delete last comma before affecting it.
		String pk = tableNoPpk.substring(0, tableNoPpk.length()-1);
		ppkDefinitions.setProperty(tableName,pk);		
		return ppkDefinitions;
	}

	private void createPrimaryKeyList(Properties ppkDefinitions,
			StringBuilder columnList, String tableName, Column[] primaryKeys) {
		for(Column c:primaryKeys){
			columnList.append(c.getColumnName()).append(",");
		}
		columnList.setLength(Math.max(0, columnList.length()-1));
		ppkDefinitions.setProperty(tableName,
				columnList.toString());
		columnList.setLength(0);
	}

}