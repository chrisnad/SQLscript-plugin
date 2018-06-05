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

import java.util.HashSet;
import java.util.Set;

import org.dbunit.dataset.filter.DefaultTableFilter;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * SAX {@link ContentHandler} for the filter XML parsing. This object is
 * statefull, thus <strong>NOT thread-safe</strong>.
 * 
 * @author edegenetais
 * 
 */
public class FilterSaxHandler implements ContentHandler {
	
	private static final String TABLE_REGEX_NAME = "tableRegex";

	private static final String FILTER_OUT_P_KS_NAME = "filterOutPKs";

	private static final String FILTER_OUT_TIMESTAMPS_NAME = "filterOutTimestamps";

	private StringBuilder currentExpression = new StringBuilder();

	private DefaultTableFilter tableFilter;
	
	private boolean hidePk;

	private Locator currentLocation;
	
	public Locator getCurrentLocation() {
		return currentLocation;
	}

	private Set<String> timeTypesSet=new HashSet<String>();
	
	/** Builder to encapsulate column filter creation. Made package accessible for testability. */
	private ColumnFilterBuilder columnFilterBuilder=new ColumnFilterBuilder();

	/** Current table regex for column filter building. */
	private String currentTableRegex;

	/** localName of the tableExclude tag. */
	private static final String TABLE_EXCLUDE_NAME = "tableExclude";

	private static final String TABLE_INCLUDE_NAME = "tableInclude";

	private static final String COLUMN_EXCLUDE_NAME = "columnExclude";

	private static final String COLUMN_INCLUDE_NAME = "columnInclude";

	// service interface
	/**
	 * @return the generated table filter.
	 */
	public ITableFilter getTableFilter() {
		return tableFilter;
	}

	/**
	 * @return the generated column filter.
	 */
	public IColumnFilter getColumnFilter() {
		return columnFilterBuilder.getColumnFilter();
	}

	// end service interface

	public boolean hasHidePk() {
		return hidePk;
	}

	// SAX handler interface
	@Override
	public void setDocumentLocator(Locator locator) {
		currentLocation=locator;
	}

	@Override
	public void startDocument() throws SAXException {
		tableFilter = new DefaultTableFilter();
		currentTableRegex=null;
	}

	@Override
	public void endDocument() throws SAXException {
		// nothing special to do at document end ==> noop
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// no use for our case ==> noop
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// no use for our case ==> noop
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (TABLE_INCLUDE_NAME.equals(qName)) {
			String regex = atts.getValue("", TABLE_REGEX_NAME);
			tableFilter.includeTable(regex);
			currentTableRegex = regex;
		}else if (TABLE_EXCLUDE_NAME.equals(qName)){
			tableFilter.excludeTable(atts.getValue("", TABLE_REGEX_NAME));
		}
		else if (isExpressionTag(qName)) {
			currentExpression.setLength(0);
		}else if(FILTER_OUT_P_KS_NAME.equals(qName)){
			hidePk=true;
		}else if("timeStamp".equals(qName)||"time".equals(qName)||"date".equals(qName)){
			timeTypesSet.add(qName);
		}
	}

	private boolean isExpressionTag(String qName) {
		return COLUMN_INCLUDE_NAME.equals(qName)|| COLUMN_EXCLUDE_NAME.equals(qName);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (TABLE_INCLUDE_NAME.equals(qName)) {
			currentTableRegex = null;
		} else if (COLUMN_INCLUDE_NAME.equals(qName)) {
			columnFilterBuilder.includeColumn(currentTableRegex,
					currentExpression.toString());
		} else if (COLUMN_EXCLUDE_NAME.equals(qName)) {
			columnFilterBuilder.excludeColumn(currentTableRegex,
					currentExpression.toString());
		} else if (FILTER_OUT_TIMESTAMPS_NAME.equals(qName)) {
			columnFilterBuilder.addTimestampExcludeFilter(this.timeTypesSet);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		currentExpression.append(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// we don't care about 'em in filter data ==> noop
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// none in filter data ==> noop
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// none in filter data ==> noop
	}
	// end SAX handler interface
}
