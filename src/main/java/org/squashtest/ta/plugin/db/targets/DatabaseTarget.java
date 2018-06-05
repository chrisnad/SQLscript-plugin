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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.annotations.TATarget;
import org.squashtest.ta.framework.components.Target;
import org.squashtest.ta.plugin.db.exceptions.ConnectionCloseException;
import org.squashtest.ta.plugin.db.exceptions.ConnectionOpenException;
import org.squashtest.ta.plugin.db.exceptions.ResultCollectionException;
import org.squashtest.ta.plugin.db.exceptions.StatementCreationException;
import org.squashtest.ta.plugin.db.exceptions.StatementExecutionException;
import org.squashtest.ta.plugin.db.library.sql.DatabaseMetadataExplorer;

@TATarget("database")
public class DatabaseTarget implements Target {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTarget.class);
	
	private static final String CONNECTION_OPEN_FAILED = "database : could not open connection";
	private static final String STATEMENT_CREATION_FAILED = "database : could not create statement";
	private static final String STATEMENT_EXECUTION_FAILED = "database : statement execution failed";
	private static final String RESULT_COLLECTION_FAILED = "database : could not gather the result set";
	private static final String CONNECTION_CLOSE_FAILED = "database : error occured while releasing connection";
	public static final String SQUASH_TA_DATABASE_SCHEMA_KEY = "squashtest.ta.database.schema";
	
	private DatasourceLifecycleManager manager;
	private Properties effectiveConfiguration;
	
	//Instead of a threadlocal variable I prefer an explicit handle on the connections.
	private Map<Long, Connection> threadsConnection = new HashMap<Long, Connection>(); 

	private DatabaseMetadataExplorer metadataExplorer;
	
	public DatabaseTarget(){
		super();
	}
	
	public DatabaseTarget(DatasourceLifecycleManager manager, Properties configuration){            
		this.effectiveConfiguration=configuration;
		this.manager=manager;
	}
	
	
	@Override
	public boolean init() {
		manager.init();
		return testConnection();
	}

	@Override
	public void reset() {
		Long threadId = Thread.currentThread().getId();
		Connection connection = threadsConnection.get(threadId);
		if (connection!=null){
			closeConnection(connection);
		}
	}

	@Override
	public void cleanup() {
		try{
			for (Connection con : threadsConnection.values()){
				closeConnection(con);
			}
			synchronized(this){
				if(metadataExplorer!=null){
					metadataExplorer.dispose();
					metadataExplorer=null;
				}
			}
			threadsConnection.clear();
			manager.release();
		}catch(Exception ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error("database target : errors occured during cleanup :", ex);
			}
			//but we don't want to prevent the rest of the cleanup
		}
	}
	
	
	@Override
	public Properties getConfiguration() {
		return effectiveConfiguration;
	}
	
	public Connection getConnection() throws ConnectionOpenException{
		try{
			Long threadId = Thread.currentThread().getId();
			Connection connection = threadsConnection.get(threadId);
			if ((connection==null) || (connection.isClosed())){
				connection = manager.getDatasource().getConnection();
				threadsConnection.put(threadId, connection);
			}
			return connection;
		}catch(SQLException ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error(CONNECTION_OPEN_FAILED, ex);
			}
			throw new ConnectionOpenException(CONNECTION_OPEN_FAILED+". "+ex.getMessage(), ex);
		}
	}
	
	protected Statement createStatement(Connection connection) throws StatementCreationException{
		try{
			return connection.createStatement();
		}catch(SQLException ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error(STATEMENT_CREATION_FAILED, ex);
			}
			throw new StatementCreationException(STATEMENT_CREATION_FAILED+". "+ex.getMessage(), ex);
		}
	}
	
	protected boolean executeStatement(Statement statement, String sql) throws StatementExecutionException{
		try{
			return statement.execute(sql);
		}catch(SQLException ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error(STATEMENT_EXECUTION_FAILED, ex);
			}
			throw new StatementExecutionException(STATEMENT_EXECUTION_FAILED+". "+ex.getMessage(), ex);
		}
	}
	
	protected int[] executeBatch(Statement statement, List<String> batch){

		try{
			Connection connection = getConnection();
			connection.setAutoCommit(false);
			
			for (String instruction : batch){
				LOGGER.debug("execution of the query: "+instruction);
				statement.addBatch(instruction);
			}
			
			int res[] = statement.executeBatch();
			
			connection.commit();
			connection.setAutoCommit(true);
			
			return res;
		}catch(SQLException ex){
			if (LOGGER.isErrorEnabled()){
				LOGGER.error(STATEMENT_EXECUTION_FAILED, ex);
			}
			throw new StatementExecutionException(STATEMENT_EXECUTION_FAILED+". "+ex.getMessage(), ex);
		}
	}
	
	protected ResultSet collectResults(Statement statement) throws ResultCollectionException{
		try {
			return statement.getResultSet();
		} catch (SQLException ex) {
			if (LOGGER.isErrorEnabled()){
				LOGGER.error(RESULT_COLLECTION_FAILED, ex);
			}
			throw new ResultCollectionException(RESULT_COLLECTION_FAILED+". "+ex.getMessage(), ex);
		}
	}
	
	protected void closeConnection(Connection connection) throws ConnectionCloseException{
		try{
			if (connection!=null){
				connection.close();
			}
		}catch(SQLException ex){
			if (LOGGER.isWarnEnabled()){
				LOGGER.warn(CONNECTION_CLOSE_FAILED, ex);
			}
		}
	}

	
	/**
	 * <p>Will execute a sql string and return a {@link ResultSet} if the given sql string if it was actually a query (select), or null
	 * for other operations (insert, update etc).</p>
	 * <p>The sql string will be executed with no flag whatsoever : if you need finer tuning you should just get the connection and do the job yourself.</p> 
	 * 
	 * @param sqlString : a preconfigured, ready to use sql string
	 * @return ResultSet : grouping the result
	 * @throws ConnectionOpenException
	 * @throws StatementCreationException
	 * @throws StatementExecutionException
	 * @throws ConnectionCloseException
	 */
	public ResultSet execute(String sqlString) throws ConnectionOpenException, 
																 StatementCreationException, 
																 StatementExecutionException,
																 ResultCollectionException, 
																 ConnectionCloseException{
		LOGGER.debug("execution of the query: "+sqlString);
		Connection connection = getConnection();		
		
		Statement statement = createStatement(connection);				
		boolean res = executeStatement(statement, sqlString);	
		
		if(res){
			return collectResults(statement);
		}else{
			return null;
		}
		
		//note that we don't close the connection here. Open connections will be closed when the engine call reset() or cleanup()

	}
	
	
	public int[] execute(List<String> batch) throws ConnectionOpenException,
                                                        StatementCreationException,
                                                        StatementExecutionException,
                                                        ResultCollectionException,
                                                        ConnectionCloseException{
		Connection connection = getConnection();		
		Statement statement = createStatement(connection);				
		return executeBatch(statement, batch);	

		//note that we don't close the connection here. Open connections will be closed when the engine call reset() or cleanup()
	}

	/**
	 * Get the list of primary keys for a table. If the table has a composite
	 * key, the list is ordered like the columns in the composite key, as far as
	 * compatibility goes.
	 * 
	 * @param table the unqualified name of the table.
	 * @return a list of primary keys.
	 * @throws SQLException in case a database access to collect metadata fails.
	 */
	public synchronized DatabaseMetadataExplorer getMetadataExplorer() throws SQLException{
		if (metadataExplorer == null) {
			/*
			 * will escape the global connection management, but see the call to
			 * metadataexplorer.dispose() cleanup method
			 */
			Connection connection = manager.getDatasource().getConnection();
			DatabaseMetaData metadata = connection.getMetaData();
			metadataExplorer=new DatabaseMetadataExplorer(metadata, getConfiguration().getProperty(SQUASH_TA_DATABASE_SCHEMA_KEY));
		}
		return metadataExplorer;
	}
	
	//We try a get connection
	private boolean testConnection() {
		try{
			getConnection();
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	interface DatasourceLifecycleManager{
		void init();
		DataSource getDatasource();
		void release();
	}

}
