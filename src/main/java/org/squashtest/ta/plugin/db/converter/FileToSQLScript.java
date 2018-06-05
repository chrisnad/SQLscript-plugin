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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.core.tools.OptionsReader;
import org.squashtest.ta.core.tools.io.StorageSizeUnit;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;
import org.squashtest.ta.framework.tools.ComponentRepresentation;
import org.squashtest.ta.plugin.db.library.sql.SQLFormatUtils;
import org.squashtest.ta.plugin.db.resources.SQLScript;


@TAResourceConverter("script")
public class FileToSQLScript extends AbstractFileToSQL
        implements ResourceConverter<FileResource, SQLScript> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileToSQLScript.class);

	public FileToSQLScript(){
		super();
	}
	
	@Override
	public float rateRelevance(FileResource input){
		//TODO : maybe run a SQL parser on that
		return 0.5f;
	}

	@Override
	public void addConfiguration(Collection<Resource<?>> configuration) {
		for(Resource<?> resource : configuration)  {
            if (resource instanceof FileResource) {
                extractEncoding((FileResource) resource);
                extractDelimiter((FileResource) resource);
            } else {
                LOGGER.warn("Ignoring unexpected configuration resource {}", resource);
            }
        }
	}

	@Override
	public SQLScript convert(FileResource resource) {
            SQLScript resultQuery = null;
            Charset cs = getCharsetFromConfiguration();
		try (
                FileInputStream fis = new FileInputStream(resource.getFile());
                InputStreamReader isr = new InputStreamReader(fis, cs);
                BufferedReader br = new BufferedReader(isr);
            ){

			String query = "";
                        List<String> script = new ArrayList<String>();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("--")){
					//we take away the comments behind --
					line = line.substring(0,line.indexOf("--"));
				}
                                query = SQLFormatUtils.stripComments(query);
                                query = SQLFormatUtils.stripWhiteLines(query);
				query = query.concat(" ").concat(line);
                                script.add(query);
                                query = "";
			}
			try{
			resultQuery = new SQLScript(script);
			}catch(BadDataException bde){
				String message = "The SQL query "+resource.getFile().getName()+"can not contain more than one query. If you want to execute several queries with a single file, convert it to a SQL script";
				throw new BadDataException(message, bde);
			}
		} catch (FileNotFoundException fnfe) {
			throw new BadDataException("file not found!!!!!\n",fnfe);
		} catch (IOException ioe) {
			throw new InstructionRuntimeException("Resource convert I/O error",ioe);
		}
        
		return resultQuery;
	}

	@Override
	public void cleanUp() {
		// no cleanup needed
	}

    @Override
    protected String getComponentRepresentation() {
        return new ComponentRepresentation(this).toString();
    }
	
}
