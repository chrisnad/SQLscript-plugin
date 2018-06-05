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
package org.squashtest.ta.plugin.db.resources;

import org.dbunit.dataset.Column;
import org.squashtest.ta.framework.annotations.TAResource;
import org.squashtest.ta.framework.components.Resource;
import org.squashtest.ta.plugin.db.library.dbunit.PPKFilter;

/**
 * DbUnitPseudoPrimaryKey filter .
 * @author fgaillard
 *
 */
@TAResource("conf.dbunit.ppk")
public class DbUnitPPKFilter implements Resource<DbUnitPPKFilter> {

	private PPKFilter currentFilter;

	/**
	 * Default constructor for Spring enumeration only.
	 */
	public DbUnitPPKFilter(){}
	
	public DbUnitPPKFilter(PPKFilter filter){
		currentFilter = filter;
	}
	
	@Override
	public DbUnitPPKFilter copy() {
		return new DbUnitPPKFilter(this.currentFilter);
	}

	@Override
	public void cleanUp() {
		//noop
	}
	
	public boolean accept(String pTableName, Column pColumn) {
        return currentFilter.accept(pTableName, pColumn);
    }
	
	public PPKFilter getFilter(){
		return this.currentFilter;
	}
}
