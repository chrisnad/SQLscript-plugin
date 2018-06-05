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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;

/**
 * Class to merge additional data into an existing {@link ITable}.
 * @author edegenetais
 *
 */
public class AugmentedTable implements ITable {
	private ITable augmentedTable;
	private List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

	public AugmentedTable(ITable originalTable) {
		this.augmentedTable = originalTable;
	}

	@Override
	public ITableMetaData getTableMetaData() {
		return augmentedTable.getTableMetaData();
	}

	@Override
	public int getRowCount() {
		return augmentedTable.getRowCount() + values.size();
	}

	@Override
	public Object getValue(int row, String column) throws DataSetException {
		if (row < 0) {
			throw new IllegalArgumentException("Negative index: " + row);
		}
		
		checkColumnExists(column);
		
		if (row < augmentedTable.getRowCount() //if within original data, fetch'em from the original table
				|| row > getRowCount()			//if over augmented size, let the original ITable handle the illegal row index its way...
				) {
			return augmentedTable.getValue(row, column);
		} else {
			return values.get(additionalDataRowId(row)).get(column);
		}
	}

	/**
	 * Register value for the table. Please note that leaving gaps in the table
	 * is not allowed.
	 * 
	 * @param row
	 *            rowindex in the merged data (original table+additional data)
	 * @param columnName
	 *            name of the column in which the new value must appear.
	 * @param value
	 *            value to set.
	 * @return the previous value for this column and row, if any.
	 * @throws DataSetException
	 *             if some error occurs while checking arguments against the
	 *             original table characteristics. May happen if the given
	 *             column name does not exist in the original table.
	 * @throws IllegalArgumentException
	 *             if the row is not within range (registering is allowed for
	 *             rows after original data if it creates no gap, i.e. the given
	 *             row number is within existing additional rows, or creates a
	 *             new row immediately after the last existing one).
	 */
	public Object registerValue(int row, String columnName, Object value)
			throws DataSetException {
		if (row < 0) {
			throw new IllegalArgumentException("Negative index: " + row);
		}
		checkColumnExists(columnName);

			int addedRowIndex=additionalDataRowId(row);
			if (addedRowIndex < 0) {
				throw new IllegalArgumentException("Row index collides with existing row from original table: " + row);
			} else if (addedRowIndex < values.size()) {
				return values.get(addedRowIndex).put(columnName, value);
			} else if (addedRowIndex == values.size()) {
				return createNewRow(columnName, value);
			} else {
				throw new IllegalArgumentException("Row index " + row
						+ " leaves a gap in merged table, given current size ("
						+ (augmentedTable.getRowCount()+values.size()) + ")");
			}

	}

	private Object createNewRow(String columnName, Object value)
			throws DataSetException {
		HashMap<String, Object> rowData = new HashMap<String, Object>(
				augmentedTable.getTableMetaData().getColumns().length);
		values.add(rowData);
		rowData.put(columnName, value);
		return null;
	}

	private int additionalDataRowId(int row) {
		return row-augmentedTable.getRowCount();
	}

	private void checkColumnExists(String columnName) throws DataSetException,
			NoSuchColumnException {
		Column[] existingColumns = augmentedTable.getTableMetaData()
				.getColumns();
		boolean searching = true;
		for (int index = 0; searching && index < existingColumns.length; index++) {
			if (existingColumns[index].getColumnName().equalsIgnoreCase(columnName)) {
				searching = false;
			}
		}
		if (searching) {
			throw new NoSuchColumnException(augmentedTable.getTableMetaData()
					.getTableName(), columnName);
		}
	}
}
