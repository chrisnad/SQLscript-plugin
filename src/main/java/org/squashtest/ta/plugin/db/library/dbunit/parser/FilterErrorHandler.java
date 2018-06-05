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
package org.squashtest.ta.plugin.db.library.dbunit.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.exception.BadDataException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FilterErrorHandler implements ErrorHandler{

	private static final Logger LOGGER=LoggerFactory.getLogger(FilterErrorHandler.class);
	
	private FilterSaxHandler handler;
	
	public FilterErrorHandler(FilterSaxHandler handler) {
		this.handler=handler;
	}
	
	@Override
	public void warning(SAXParseException exception) throws SAXException {
		if(LOGGER.isWarnEnabled()){
			LOGGER.warn("SAX warning issued near {} while parsing filter configuration:{}",locationToString(),exception.getMessage());
		}
	}

	private String locationToString() {
		StringBuilder location=new StringBuilder();
		if(handler.getCurrentLocation()!=null){
			location.append(" near line ").append(handler.getCurrentLocation().getLineNumber());
			location.append(";column ").append(handler.getCurrentLocation().getColumnNumber());
		}
		return location.toString();
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		throw new BadDataException("Invalid filter XML, parsing error"+locationToString(), exception);
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw new BadDataException("Fatal parsing error on filter XML data"+locationToString(),exception);
	}

}
