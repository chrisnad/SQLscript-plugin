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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.plugin.db.library.dbunit.parser.FilterConfiguration;
import org.squashtest.ta.plugin.db.library.dbunit.parser.FilterParser;
import org.squashtest.ta.plugin.db.resources.DbUnitFilterResource;

/**
 * Validates a file against the squash dataset filter configuration schema, and
 * converts the related {@link FileResource} into a {@link DbUnitFilterResource}
 * resource. Here are two example configurations: 
 * <ul><li>Table inclusion based filter:
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
<filter>
	<tableInclude tableRegex="table1">
		<columnInclude>toto</columnInclude>
		<columnInclude>tutu</columnInclude>
	</tableInclude>
	<tableInclude>
		<columnExclude>tutu</columnExclude>
	</tableInclude>
	<columnExclude>id</columnExclude>
</filter>}
 * </pre>
 * </li>
 * <li>Table exclusion based filter:
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
<filter>
  <filterOutTimestamps/>
  <filterOutPKs/>
  <tableExclude>tableExclude</tableExclude>
  <columnExclude>columnExclude</columnExclude>
</filter>

 * }
 * </pre>
 * </li></ul>
 * NB: you cannot have both includeTable and excludeTable elements in the same filter.
 * @author edegenetais
 * 
 */
@TAResourceConverter("filter")
public class XmlToDbunitFilterConverter implements ResourceConverter<FileResource, DbUnitFilterResource> {

	private static final Logger LOGGER=LoggerFactory.getLogger(XmlToDbunitFilterConverter.class);
	
	/** Noarg constructor for Spring */
	public XmlToDbunitFilterConverter() {}
	
	@Override
	public float rateRelevance(FileResource input) {
		return 0.5f;
	}

	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		//no configuration awaited ==> let's warn if some is transmitted
		if(LOGGER.isWarnEnabled() && configuration.size()>0){
			LOGGER.warn("Configuration transmitted to XmlToDbUnitFilter is useles ("+configuration.size()+" resources)");
		}
	}

	@Override
	public DbUnitFilterResource convert(FileResource resource) {
		try {File filterDescriptor=resource.getFile();
			InputStream filterStream = new FileInputStream(filterDescriptor);
			FilterParser parser=new FilterParser();
			FilterConfiguration configuration=parser.parse(filterStream);
			return new DbUnitFilterResource(configuration,null);
		} catch (FileNotFoundException e) {
			throw new BadDataException("Filter descriptor resource content not found.", e);
		}
	}

	@Override
	public void cleanUp() {
		//noop, GC should be enough.
	}

}
