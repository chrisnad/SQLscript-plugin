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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.squashtest.ta.framework.exception.InstructionRuntimeException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This parser class parses filter XML data and produces a filter set
 * accordingly.
 * 
 * @author edegenetais
 * 
 */
public class FilterParser {
	
	private SAXParserFactory parserFactory=SAXParserFactory.newInstance();
	private SchemaFactory schemaFactory=SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	
	
	/** handler factories for testability */
	static class FilterSaxHandlerFactory {
		public FilterSaxHandler newInstance(){
			return new FilterSaxHandler();
		}
	}
	static class FilterErrorHandlerFactory{
		public FilterErrorHandler newInstance(FilterSaxHandler handler){
			return new FilterErrorHandler(handler);
		}
	}
	private FilterSaxHandlerFactory handlerFactory=new FilterSaxHandlerFactory();
	private FilterErrorHandlerFactory errorHandlerFactory=new FilterErrorHandlerFactory();
	
	/**
	 * Creates a valid parser.
	 */
	public FilterParser() {
		try {
			URL schemaURL=getClass().getResource("squash-filter-config.xsd");
			Schema dbunitFilterSchema=schemaFactory.newSchema(schemaURL);
			parserFactory.setSchema(dbunitFilterSchema);
		} catch (SAXException e) {
			throw new InstructionRuntimeException("Could not create dbunit filter XML parser", e);
		}
	}
	
	public FilterConfiguration parse(InputStream source){
		try {
			//create parser
			SAXParser parser=parserFactory.newSAXParser();
			XMLReader reader=parser.getXMLReader();
			FilterSaxHandler handler = handlerFactory.newInstance();
			reader.setContentHandler(handler);
			reader.setErrorHandler(errorHandlerFactory.newInstance(handler));
			
			//parse
			InputSource xmlSource=new InputSource(source);
			reader.parse(xmlSource);
			
			//extract configuration and yield it
			FilterConfiguration configuration = new FilterConfiguration(
					handler.getTableFilter(), handler.getColumnFilter(),
					handler.hasHidePk());
			return configuration;
			
		} catch (ParserConfigurationException e) {
			throw new InstructionRuntimeException(e);
		} catch (SAXException e) {
			throw new InstructionRuntimeException(e);
		} catch (IOException e) {
			throw new InstructionRuntimeException(e);
		}
	}
	
}
