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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.core.tools.OptionsReader;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.exception.IllegalConfigurationException;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;

/**
 * Base Classe for {@link FileToSQL} {@link FileToSQLScript} converters.
 * It stores the encoding sql query information and prodives 
 * <ul>
 * <li>{@link AbstractFileToSQL#extractEncoding(org.squashtest.ta.framework.components.FileResource) } to extract the encoding from a configuration file.</li>
 * <li>{@link AbstractFileToSQL#getCharsetFromConfiguration()} to get the correct {@link Charset} to use.</li>
 * </ul>
 * @author fgautier
 */
public abstract class AbstractFileToSQL {
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    /** The option reader used to read the option file configuration.*/
    private static final OptionsReader OPTIONS_READER = OptionsReader.BASIC_READER;
    /** Key to access the SQL query file encoding in the option fileresource.*/
    private static final String ENCODING_OPTION_KEY = "encoding";
    /** Key to access the SQL query file encoding in the option fileresource.*/
    private static final String DELIMITER_OPTION_KEY = "delimiter";
    /** The encoding of the file describing the query.*/
    private String queryEncoding;

    public AbstractFileToSQL() {}
    
    /**
     * Exctracts the query file encoding from an Option formatted {@link FileResource}
     * containing the ENCODING_OPTION_KEY.
     * @param resource The file resource used as a configuration.
     */
    protected void extractEncoding(FileResource resource) throws InstructionRuntimeException {
        final File optionFile = resource.getFile();
        try {
            if (OPTIONS_READER.isOptions(optionFile) ) {
                final Map<String, String> optionMap = OPTIONS_READER.getFilteredOptions(optionFile);
                if (optionMap.containsKey(ENCODING_OPTION_KEY)) {
                    if (queryEncoding==null) {
                        queryEncoding = optionMap.get(ENCODING_OPTION_KEY);
                        LOGGER.debug("{} defined as query encoding", queryEncoding);
                    } else {
                        LOGGER.warn("Ignoring unexpected extra valid configuration resource {}. Encoding {} is already set",resource, queryEncoding);
                    }
                } else {
                    LOGGER.warn("Ignoring option file resource {}. A valid {} converter configuration must contain the key {}", resource, getComponentRepresentation(), ENCODING_OPTION_KEY);
                }
            } else {
                LOGGER.warn("Ignoring non optionfile formated file resource {}", resource);
            }
        } catch (IOException e) {
            throw new InstructionRuntimeException("Impossible to read file encapsulated in FileResource " + resource, e);
        }
    }


/////////////////////////////////////////////////////////// REMOVE THIS BITCH //////////////////////////////////////////////////////////
    /**
     * Exctracts the delimiter file from an Option formatted {@link FileResource}
     * containing the DELIMITER_OPTION_KEY.
     * @param resource The file resource used as a configuration.
     */
    protected void extractDelimiter(FileResource resource) throws InstructionRuntimeException {
        final File optionFile = resource.getFile();
        try {
            if (OPTIONS_READER.isOptions(optionFile) ) {
                final Map<String, String> optionMap = OPTIONS_READER.getFilteredOptions(optionFile);
                if (optionMap.containsKey(DELIMITER_OPTION_KEY)) {
System.out.println(" 33333333333333333333333333333333333333333333333        " + optionMap.get(DELIMITER_OPTION_KEY));
                } else {
                    LOGGER.warn("Ignoring option file resource {}. A valid {} converter configuration must contain the key {}", resource, getComponentRepresentation(), ENCODING_OPTION_KEY);
                }
            } else {
                LOGGER.warn("Ignoring non optionfile formated file resource {}", resource);
            }
        } catch (IOException e) {
            throw new InstructionRuntimeException("Impossible to read file encapsulated in FileResource " + resource, e);
        }
    }
/////////////////////////////////////////////////////////// REMOVE THIS BITCH //////////////////////////////////////////////////////////
	
    /** 
     * Uses given encoding configuration to define what charset to use
     * @return the Charset to use to configure the streamreader .
     */
    protected Charset getCharsetFromConfiguration() {
        Charset cs;
        if (queryEncoding != null) {
            try {
                cs=Charset.forName(queryEncoding);
                LOGGER.debug("Using specified encoding " + cs.displayName());
            } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
                throw new IllegalConfigurationException(queryEncoding + " is not supported.", e);
            }
        } else {
            cs=Charset.defaultCharset();
            LOGGER.debug("Unsing standard encoding " + cs.displayName());
        }
        
        return cs;
    }
    
    /** 
     * Display name of the component for logging purposes.
     * @return The display name of the implemented converter to use in log messages.
     */
    protected abstract String getComponentRepresentation();
}
