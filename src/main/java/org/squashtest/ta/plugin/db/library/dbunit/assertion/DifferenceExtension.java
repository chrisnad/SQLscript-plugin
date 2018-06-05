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
import java.util.List;

import org.dbunit.assertion.Difference;
import org.dbunit.dataset.ITable;

public class DifferenceExtension extends Difference {

	private int possibleRow;
	private List<String> primaryKeysName;
	private PotentialMatchStatus status;
	
	public DifferenceExtension(ITable expectedTable, ITable actualTable, int expectedRow)
	{
		super(expectedTable, actualTable, expectedRow, " - ", " - ", " - ");
		status = PotentialMatchStatus.DISABLED;
		primaryKeysName = new ArrayList<String>();
	}
	
	public void addManyPotentialMatch(int potentialRowIndex, List<String> primaryKeysName) {
		this.possibleRow=potentialRowIndex;
		status=PotentialMatchStatus.MANY_MATCH;
		this.primaryKeysName=primaryKeysName;
	}
	
	public void addOnePotentialMatch(int potentialRowIndex, List<String> primaryKeysName) {
		this.possibleRow=potentialRowIndex;
		status=PotentialMatchStatus.ONE_MATCH;
		this.primaryKeysName=primaryKeysName;
	}
	
	public void addNoPotentialMatch( List<String> primaryKeysName) {
		status=PotentialMatchStatus.NO_MATCH;
		this.primaryKeysName=primaryKeysName;
	}

	public int getPossibleRowIndex() {
		return possibleRow;
	}
	
	public List<String> getPrimaryKeys() {
		return primaryKeysName;
	}

	public DifferenceExtension.PotentialMatchStatus getPotentialMatchstatus() {
		return status;
	}
	
	public boolean isPotentialMatchEnable(){
		boolean enable = false;
		if(!status.equals(PotentialMatchStatus.DISABLED)){
			enable = true;
		}
		return enable;
	}
	
	
	public enum PotentialMatchStatus {
		DISABLED, NO_MATCH, ONE_MATCH, MANY_MATCH;
	}
	
	
	
}
