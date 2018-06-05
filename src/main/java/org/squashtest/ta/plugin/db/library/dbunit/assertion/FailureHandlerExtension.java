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
package org.squashtest.ta.plugin.db.library.dbunit.assertion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.assertion.DefaultFailureHandler;
import org.dbunit.assertion.Difference;

public class FailureHandlerExtension extends DefaultFailureHandler {
	
	private Map<String, List<DifferenceExtension>> mapTableDiffList = new HashMap<String, List<DifferenceExtension>>();
    
    public void handle(Difference diff) 
    {
        if(DifferenceExtension.class.isAssignableFrom(diff.getClass())){
        	DifferenceExtension diffExt = (DifferenceExtension) diff;
        	String tableName = diff.getActualTable().getTableMetaData().getTableName();
        	if(mapTableDiffList.containsKey(tableName)){
        		List<DifferenceExtension> listDifferenceExtensions = mapTableDiffList.get(tableName);
        		listDifferenceExtensions.add(diffExt);
        	}else{
        		List<DifferenceExtension> list = new ArrayList<DifferenceExtension>();
        		list.add(diffExt);
        		mapTableDiffList.put(tableName, list);
        	}
        }
    }

    /**
     * @return The list of collected {@link DifferenceExtension}s for the table given in argument.
     */
    public List<DifferenceExtension> getDiffList(String tableName) 
    {
        return mapTableDiffList.get(tableName);
    }
    
    public Map<String, List<DifferenceExtension>> getMap() {
		return mapTableDiffList;
	}
    
    public int getSize() {
		return mapTableDiffList.size();
	}

}
