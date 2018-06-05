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
package org.squashtest.ta.plugin.db.commands;

import java.sql.SQLException;
import java.util.Collection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.ta.core.tools.ExceptionLogger;
import org.squashtest.ta.framework.annotations.TACommand;
import org.squashtest.ta.framework.components.Command;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.framework.exception.InstructionRuntimeException;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;

/**
 * Common behavior for both the legacy {@link LegacyDbunitDatabaseDumpCommand} and new {@link DbunitDatabaseDumpCommand}
 * versions of the dbunit database dump command.
 * 
 * @author edegenetais
 * 
 */
@TACommand("get.all")
public abstract class AbstractDbunitDatabaseDumpCommand extends AbstractDbUnitCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbunitDatabaseDumpCommand.class);
    private static final ExceptionLogger EXCEPTION_LOGGER = new ExceptionLogger(DbunitDatabaseDumpCommand.class,
            InstructionRuntimeException.class);

    /**
     * Implementation for the {@link Command#addConfiguration()} method (only subclasses effectively implement the
     * {@link Command} interface).
     */
    public void addConfiguration(Collection<Resource<?>> configuration) {
        putConfiguration(configuration);
    }

    /**
     * Implementation for the {@link Command#apply()} method (only subclasses effectively implement the {@link Command}
     * interface).
     */
    public DbUnitDatasetResource apply() {

        try {
            configure();

            IDatabaseConnection connection = buildDbUnitConnection();

            IDataSet dataset = connection.createDataSet();
            DbUnitDatasetResource result = new DbUnitDatasetResource(dataset, true);

            return result;
        } catch (DatabaseUnitException ex) {
            String message = "db unit insert : an error originated from the DbUnit framework occured:";
            throw EXCEPTION_LOGGER.errAndThrow(message, ex);
        } catch (SQLException ex) {
            String message = "db unit insert : an error originated from the database occured:";
            throw EXCEPTION_LOGGER.errAndThrow(message, ex);
        }
    }

    /**
     * Implementation for the {@link Command#cleanUp()} method (only subclasses effectively implement the
     * {@link Command} interface).
     */
    public void cleanUp() {
        // nothing to do
    }

    @Override
    protected void applySpecificConfiguration(Resource<?> element) {
        LOGGER.warn("Unrecognized configuration element:" + element.toString() + " will be ignored!");
    }

}
