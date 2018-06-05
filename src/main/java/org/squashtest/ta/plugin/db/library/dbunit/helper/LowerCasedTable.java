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
package org.squashtest.ta.plugin.db.library.dbunit.helper;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.LowerCaseTableMetaData;

/**
 * Class to create an ITable with all its collumn with an undercase name from an existing table with columns having an uppercase name
 * @author fgaillard
 *
 */
public class LowerCasedTable implements ITable {
	private ITable lowerCasedTable;
	private LowerCaseTableMetaData lowerCaseMetaData;
	
	public LowerCasedTable(ITable originalTable) throws DataSetException {
		this.lowerCasedTable = originalTable;
		ITableMetaData itmd = originalTable.getTableMetaData();
		if (itmd.getPrimaryKeys() != null){
			this.lowerCaseMetaData = new LowerCaseTableMetaData(itmd.getTableName(), itmd.getColumns(), itmd.getPrimaryKeys());	
		} else {
			this.lowerCaseMetaData = new LowerCaseTableMetaData(itmd.getTableName(), itmd.getColumns(), new Column[]{});
		}
	}

	@Override
	public ITableMetaData getTableMetaData() {
		return lowerCaseMetaData;
	}

	@Override
	public int getRowCount() {
		return lowerCasedTable.getRowCount();
	}

	@Override
	public Object getValue(int row, String column) throws DataSetException {
		if (row < 0) {
			throw new IllegalArgumentException("Negative index: " + row);
		}
		return lowerCasedTable.getValue(row, column);
	}
}
