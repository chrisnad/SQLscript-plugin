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
package org.squashtest.ta.plugin.db.library.dbunit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;

public final class DatabaseOperationReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseOperationReader.class);
	
	private static final String DBU_UPDATE =  "UPDATE";
	private static final String DBU_INSERT =  "INSERT";
	private static final String DBU_DELETE =  "DELETE";
	private static final String DBU_DELETE_ALL =  "DELETE_ALL";
	private static final String DBU_TRUNCATE_TABLE =  "TRUNCATE_TABLE";
	private static final String DBU_REFRESH =  "REFRESH";
	private static final String DBU_CLEAN_INSERT =  "CLEAN_INSERT";
	private static final String DBU_NONE =  "NONE";
	
	private DatabaseOperationReader(){};
	
	/**
	 * <p>Will read a file and extract which DbUnit operation the content refers to. Will throw runtime exceptions if the file cannot be read or if the content is 
	 * inappropriate. Will not delete the file afterward.</p>
	 * 
	 *  <p>   
	 *  <strong>Valid content definition</strong> : 
	 *  	<ul>
	 *  		<li>One line that must specify the operation (subsequent lines are ignored),</li>
	 *  		<li>that line contains the database operation name.</li>
	 *  	</ul>
	 *  
	 *  	The database operation name may use either short names or fully qualified class names. If a fully qualified class name is stated
	 *  	an instance of the corresponding class will be returned. If a short name is supplied it must be one of the following (see http://www.dbunit.org/components.html) : 
	 *		
	 *		<ul>
	 *			<li>UPDATE</li>
	 *			<li>INSERT</li>
	 *			<li>DELETE</li>
	 *			<li>DELETE_ALL</li>
	 *			<li>TRUNCATE_TABLE</li>
	 *			<li>REFRESH</li>
	 *			<li>CLEAN_INSERT</li>
	 *			<li>NONE</li>
	 *  	</ul>
	 *  
	 *  	Short names are case-insensitive, qualified names are not.
	 *  </p>
	 * 
	 * @param file
	 * @return
	 */
	public static DatabaseOperation readOperation(File file){
		
		if (file==null){
			throw logAndThrow("database operation : supplied configuration file is null", null);
		}
		
		BufferedReader reader = null;
		try {
			
			reader = new BufferedReader(new FileReader(file));
			String content = reader.readLine();
			
			if (content==null){
				throw logAndThrow("DatabaseOperationReader : file '"+file.getPath()+"' is empty", null);				
			}
			
			return readOperation(content);
			
		} 
		catch (FileNotFoundException e) {
			throw logAndThrow("DatabaseOperationReader : file '"+file.getPath()+"' not found",e);
		} 
		catch (IOException e) {
			throw logAndThrow("DatabaseOperationReader : file '"+file.getPath()+"' could not be read", e);
		}
		finally{
			try{
				if (reader!=null){
					reader.close();
				}
			}
			catch(IOException ex){
				throw new InstructionRuntimeException("database operation : failed to close stream on supplied configuration file" , ex);
			}
		}
	}
	
	//I don't see a serious issue with complexity here
	public static DatabaseOperation readOperation(String name){	//NOSONAR 
		
		String sName = toShort(name).toUpperCase();
		
		DatabaseOperation operation = DatabaseOperation.UPDATE;
		
		if (sName.equals(DBU_UPDATE)){
			
			operation = DatabaseOperation.UPDATE;
			
		}else if (sName.equals(DBU_INSERT)){
			
			operation = DatabaseOperation.INSERT;
			
		}else if (sName.equals(DBU_DELETE)){
			
			operation = DatabaseOperation.DELETE;
			
		}else if (sName.equals(DBU_DELETE_ALL)){
			
			operation = DatabaseOperation.DELETE_ALL;
			
		}else if (sName.equals(DBU_TRUNCATE_TABLE)){
			
			operation = DatabaseOperation.TRUNCATE_TABLE;
			
		}else if (sName.equals(DBU_REFRESH)){
			
			operation = DatabaseOperation.REFRESH;
			
		}else if (sName.equals(DBU_CLEAN_INSERT)){
			
			operation = DatabaseOperation.CLEAN_INSERT;
			
		}else if (sName.equals(DBU_NONE)){		
			
			operation = DatabaseOperation.NONE;
			
		}else{			
			operation = instantiate(name);			
		}
		
		return operation;
	}
	
	
	private static String toShort(String name){
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex==-1){
			return name;
		}else{
			return name.substring(dotIndex);
		}		
	}
		
	private static DatabaseOperation instantiate(String name){
		DatabaseOperation operation;
		try {
			operation = (DatabaseOperation)Class.forName(name).newInstance();
			return operation;
		} catch (InstantiationException e) {
			throw logAndThrow("DatabaseOperationReader : classname '"+name+"' could not be instantiated :", e);
		} catch (IllegalAccessException e) {
			throw logAndThrow("DatabaseOperationReader : constructor for classname '"+name+"' is private or otherwise impossible to invoke:", e);		
		} catch (ClassNotFoundException e) {
			throw logAndThrow("DatabaseOperationReader : class '"+name+"' not found :", e);
		} catch(ClassCastException e){
			throw logAndThrow("DatabaseOperationReader : class '"+name+"' is not an instance of DatabaseOperation :", e);
		}		
	}
	
	private static InstructionRuntimeException logAndThrow(String message, Exception e){
		if (LOGGER.isErrorEnabled()){
			LOGGER.error(message, e);
		}
		throw new InstructionRuntimeException(message, e);
	}

}
