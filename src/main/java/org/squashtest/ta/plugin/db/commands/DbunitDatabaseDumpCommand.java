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

import org.squashtest.ta.framework.annotations.TACommand;
import org.squashtest.ta.framework.components.Command;
import org.squashtest.ta.framework.components.FileResource;
import org.squashtest.ta.framework.components.VoidResource;
import org.squashtest.ta.plugin.db.resources.DbUnitConfiguration;
import org.squashtest.ta.plugin.db.resources.DbUnitDatasetResource;
import org.squashtest.ta.plugin.db.resources.DbUnitPPKFilter;
import org.squashtest.ta.plugin.db.targets.DatabaseTarget;


/**
 * This is the new version based on {@link VoidResource}. see {@link LegacyDbunitDatabaseDumpCommand} for the legacy one with the ignored, bogus {@link FileResource} INPUT resource
 * <p><strong>description</strong> Will read-out the entire content of a {@link DatabaseTarget} as a {@link DbUnitDatasetResource}. This command requires no input resource.<p>
 * 
 * <p><strong>Configuration (optional) : 
 * 	<ul>
 * 		<li>{@link DbUnitConfiguration} : additional configuration for the DbUnit connection. </li>
 * 		<li>{@link DbUnitPPKFilter} : A Pseudo Primary Key filter that will override the one supplied in the dbconfiguration if any. </li>
 *  </ul>
 * </p>
 * 
 * <p><strong>DSL example : </strong>EXECUTE get.all WITH $() ON my.db USING my.dbu.ppk, my.dbu.conf, AS my.result.dataset</p>
 * 
 * @author bsiri
 *
 */

@TACommand("get.all")
public class DbunitDatabaseDumpCommand extends AbstractDbunitDatabaseDumpCommand implements Command<VoidResource, DatabaseTarget> {
	
	@Override
	public void setResource(VoidResource resource) {
	}

	//this may seem useless, but is required for correct command OUTPUT type detection
	@Override
	public DbUnitDatasetResource apply() {
		return super.apply();
	}
	
}
