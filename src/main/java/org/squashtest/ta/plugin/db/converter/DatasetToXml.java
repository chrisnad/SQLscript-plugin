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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.squashtest.ta.plugin.db.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.framework.annotations.TAResourceConverter;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.components.ResourceConverter;
import org.squashtest.ta.framework.exception.BadDataException;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;
import org.squashtest.ta.framework.tools.ComponentRepresentation;
import org.squashtest.ta.framework.tools.TempDir;
import org.squashtest.ta.plugin.commons.resources.XMLResource;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;

/**
 * This converter allows data extracted from DB to be injected into other resources.
 * @author edegenetais
 */
@TAResourceConverter("dbu.xml")
public class DatasetToXml implements ResourceConverter<DbUnitDatasetResource, XMLResource>{

    private static final Logger LOGGER=LoggerFactory.getLogger(DatasetToXml.class);
    
    @Override
    public float rateRelevance(DbUnitDatasetResource input) {
        return 0.5f;
    }

    @Override
    public void addConfiguration(Collection<Resource<?>> configuration) {
        //Noop
        if(configuration.size()>0){
                LOGGER.warn("{} Ignoring {} configuration elements. No configuration for this converter.", new ComponentRepresentation(this),configuration.size());
        }
    }

    @Override
    public XMLResource convert(DbUnitDatasetResource resource) {
        try {
            File xmlDataset=File.createTempFile("dbu-ds", ".xml",TempDir.getExecutionTempDir());
            IDataSet ds=resource.getDataset();
            try(
                final OutputStreamWriter xmlOutputWriter = new OutputStreamWriter(new FileOutputStream(xmlDataset),"UTF-8");
                    ){
                FlatXmlDataSet.write(ds, xmlOutputWriter, "UTF-8");
                return new XMLResource(xmlDataset);
            }
        } catch (IOException ex) {
            throw new InstructionRuntimeException("Writing DbUnit dataset to xml failed on I/O.", ex);
        } catch (DataSetException ex) {
            throw new BadDataException("Dataset to Xml conversion failed", ex);
        }
    }

    @Override
    public void cleanUp() {
        //noop
    }
    
}
