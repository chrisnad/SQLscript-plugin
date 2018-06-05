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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dbunit.database.CachedResultSetTable;
import org.dbunit.database.ForwardOnlyResultSetTable;
import org.dbunit.database.IResultSetTable;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.core.tools.OptionsReader;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.framework.exception.IllegalConfigurationException;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;
import org.squashtest.ta.plugin.db.resources.SQLResultSet;


/**
 * <p>This class creates a DbUnit DataSet using a previous sql result</p>. 
 * 
 * 
 * <p>Configuration : 
 * 	<ul>
 * 		<li>{@link FileResource} : a FileResource which entries are comma separated pairs of 
 * 			<lt;key:value&gt; (note that column ':' is the separator) (see below) If not supplied, defaults to "default" </li> 
 * 	</ul>
 * </p>
 * 
 * <p><strong>available options</strong>
 * 	<ul>
 * 		<li>tablename : &lt;the table name &gt;. It will state that the current result set / data set represents
 * 		the said table</li>
 * 	</ul>
 * </p>
 * 
 * <p>Be warned though that the implementation is 
 * simplistic regarding the metadata : we use here a default implementation of {@link ITableMetaData} instead of 
 * the specific {@link ResultSetTableMetaData}, which is more accurate yet cumbersome to use in the context
 * of Squash TA. The difference is that the later uses the IDatabaseConnection to fetch the informations directly 
 * from the database in order to circumvent issues from the various implementations of JDBC drivers.<p>
 * 
 * 
 * 
 * 
 * 
 * @author bsiri
 *
 */

@TAResourceConverter("dataset")
public class ResultSetToDataset implements ResourceConverter<SQLResultSet, DbUnitDatasetResource> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResultSetToDataset.class);
	
	private Collection<Resource<?>> config = new LinkedList<Resource<?>>();
	
	private String tableName="default";
	
	
	@Override
	public float rateRelevance(SQLResultSet input) {
		return 0.9f;	//almost always relevant 
	}

	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		config.addAll(configuration);
	}

	@Override
	public DbUnitDatasetResource convert(SQLResultSet resource) {
		try{
			
			findTableName();
			
			ResultSet result = resource.getResultSet();
			ITableMetaData data = buildMetadata(result);
			
			IResultSetTable table = new ForwardOnlyResultSetTable(data, result);
			table = new CachedResultSetTable(table);
			
			IDataSet dataset = new DefaultDataSet(table);
			
			//we set hasMetadata to false here because we only include the type information, but no indication about primary key status
			return new DbUnitDatasetResource(dataset,false);
			
		}catch(SQLException ex){
			throw logAndBuildException("converter from result set to dataset : an error occured while creating the dataset, caused by :", ex);
		} catch (DataSetException ex) {
			throw logAndBuildException("converter from result set to dataset : an error occured while creating the dataset, caused by :", ex);
		}
		
		
	}
	
	protected void findTableName(){

		try{
			_scanConfiguration();
		}
		catch(IllegalArgumentException ex){
			throw logAndBuildException("converter from result set to dataset : supplied options are invalid", ex);
		} 
		catch (IOException ex) {
			throw logAndBuildException("converter from result set to dataset : could not read file supplied as configuration ", ex);
		}
	}
	
	private void _scanConfiguration() throws IOException, IllegalArgumentException{
		for (Resource<?> resource : config){
			if (FileResource.class.isAssignableFrom(resource.getClass())){

				Map<String, String> options = OptionsReader.BASIC_READER.getOptions(((FileResource)resource).getFile());
				
				_setTableName(options);
			}
		}		
	}
	
	private void _setTableName(Map<String, String> options){
		String name = options.get("tablename");
		if ((name!=null) && (! name.isEmpty())) tableName=name;
		else{
			throw logAndBuildIllegalConfiguration("converter from result set to dataset : file supplied as configuration was empty", null);				
		}		
	}
	
	protected ITableMetaData buildMetadata(ResultSet resultSet){
		try{
			
			ResultSetMetaData data = resultSet.getMetaData();
			int nbcols = data.getColumnCount();
			
			List<Column> columns = new LinkedList<Column>();
			
			for (int i=1;i<=nbcols;i++){
				 String colName = data.getColumnLabel(i);
				 int sqlType = data.getColumnType(i);
				 DataType type = DataType.forSqlType(sqlType);
				 columns.add(new Column(colName, type));
			}
			
			return new DefaultTableMetaData(tableName, columns.toArray(new Column[columns.size()]));
		
		}catch(SQLException ex){
			throw logAndBuildException("converter from result set to dataset : failed to read the result set, caused by :", ex);
		} catch (DataTypeException ex) {
			throw logAndBuildException("converter from result set to dataset : the database returned a wrong SQL type for one column of the result set.", ex);
		}		
	}
	
	
	private InstructionRuntimeException logAndBuildException(String message, Exception orig){
		if (LOGGER.isErrorEnabled()){
			LOGGER.error(message, orig);
		}
		return new InstructionRuntimeException(message, orig);		
	}

	private IllegalConfigurationException logAndBuildIllegalConfiguration(String message, Exception orig){
		if (LOGGER.isErrorEnabled()){
			LOGGER.error(message, orig);
		}
		return new IllegalConfigurationException(message, orig);		
	}

	
	@Override
	public void cleanUp() {
		//nothing special
	}

	
	
}
