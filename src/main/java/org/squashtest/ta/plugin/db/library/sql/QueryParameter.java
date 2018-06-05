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
package org.squashtest.ta.plugin.db.library.sql;

public class QueryParameter {

	private String name;
	private String value;
	
	/**
	 * Constructor
	 */
	public QueryParameter(String name, String value){
		this.name = name;
		this.value = value;
	}
	
	/**
	 * @return the name of the parameter
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * sets the name of the parameter
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the value of the parameter
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * sets the value of the parameter
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
