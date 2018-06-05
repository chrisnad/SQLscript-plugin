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
package org.squashtest.ta.plugin.db.library.dbunit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.IColumnFilter;

/**
 * {@link IColumnFilter} to exclude all time related columns.
 * 
 * @author edegenetais
 * 
 */
public class NoTimestampColumnFilter implements IColumnFilter {
	/**
	 * Transcoding table with supported {@link DataType}.
	 * @author edegenetais
	 *
	 */
	public enum TimeStampTypes{
		timeStamp(DataType.TIMESTAMP),
		time(DataType.TIME),
		date(DataType.DATE);
		private DataType dataType;
		private TimeStampTypes(DataType relatedType){
			this.dataType=relatedType;
		}
		public static DataType transcode(String typeName){
				return valueOf(typeName).dataType;
		}
	}
	
	private List<DataType> targetTypes=new ArrayList<DataType>();
	
	public NoTimestampColumnFilter(Collection<String> targets) {
		for(String target:targets){
			targetTypes.add(TimeStampTypes.transcode(target));
		}
	}
	@Override
	public boolean accept(String tableName, Column column) {
		boolean accept=true;
		for(DataType type:targetTypes){
			if(type.equals(column.getDataType())){
				accept=false;
			}
		}
		return accept;
	}

}
