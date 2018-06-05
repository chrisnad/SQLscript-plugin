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
package org.squashtest.ta.plugin.db.library.dbunit.helpers;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.filter.ITableFilter;

public class YesTableFilter implements ITableFilter{

	@Override
	public boolean accept(String tableName) throws DataSetException {
		return true;
	}

	@Override
	public String[] getTableNames(IDataSet dataSet) throws DataSetException {
		return dataSet.getTableNames();
	}

	@Override
	public ITableIterator iterator(IDataSet dataSet, boolean reversed)
			throws DataSetException {
		return reversed?dataSet.reverseIterator():dataSet.iterator();
	}

}
