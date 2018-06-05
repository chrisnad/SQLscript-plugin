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
package org.squashtest.ta.plugin.db.exceptions;

@SuppressWarnings("serial")
public class ConnectionOpenException extends RuntimeException{

	public ConnectionOpenException() {
		super();
	}

	public ConnectionOpenException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ConnectionOpenException(String arg0) {
		super(arg0);
	}

	public ConnectionOpenException(Throwable arg0) {
		super(arg0);
	}

	
}
