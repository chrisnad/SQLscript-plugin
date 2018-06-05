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
package org.squashtest.ta.plugin.db.library.sql;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class SQLFormatUtils {
	
	
	/**
	 * The comments between comment symboles are deleted
	 * @param entryString
	 * @return the string without comments
	 */
	public static String stripComments(String entryString){
		if (entryString.contains("/*")){
			String[] tabedString = entryString.split("\\/\\*");
			String takeAwayComment="";
			for (int i=0; i<tabedString.length; i++) {
				if (i==0){
					takeAwayComment = takeAwayComment.concat(tabedString[0]);
				}
				else {
					if (tabedString[i].contains("*/")){
						//We strip the text contained before the */ characters and replace them with an empty string
						takeAwayComment = takeAwayComment.concat(tabedString[i].substring(tabedString[i].indexOf("*/")).replace("*/", ""));
					}
				}
			}
			return takeAwayComment;
		} else {
			//no comments in the string we return it as it is
			return entryString;
		}
	}
	

	/**
	 * the white lines contained in the file have all been concatenated to the file
	 * this method is recursive to replace all the double spaces "  " by single spaces until there are no more double spaces
	 * @param entryString
	 * @return
	 */
	public static  String stripWhiteLines(String entryString){
		String resultString = entryString.replace("  ", " ");
		if (entryString.contains("  ")){
			resultString = stripWhiteLines(resultString);
		}
		return resultString;
	}
	
	
	/*Here is the problem: if we only split the query with the semicolon, 
	 * it causes an issue when a semicolon is used inside a value (not as the end of a query).
	 * (see Mantis 1930)
	 */
	/**
	 * Split a script in several sql queries
	 * @param script : the script to analyse
	 * @return a List<String> of queries
	 */
	public static List<String> splitInstructions(String script) {
		List<String> instructions = new ArrayList<String>();
		String query = "";
		boolean insideValue = false;
		for (char character : script.toCharArray()){
			//we read each character, and add it into a String
			query += character;
			
			if (character == '\'' || character == '\"' ){
				/* every time we meet a quotation mark, this boolean change its value
				 * - we are not inside a value? it is an open quotation mark, and now we are
				 * - we are inside a value? It is a closed quotation mark, and we are note anymore*/	
				insideValue = insideValue?false:true;
				
			}else if (character == ';' && !insideValue){
				/* if we meet a semicolon that is not inside quotation marks, it is the end of the instruction.
				 * (if it is inside quotation marks, that is just a regular character of the concerned value) */
				instructions = addToList(query, instructions);
				query = "";
			}
		}
		instructions = addToList(query, instructions);
		return instructions;
	}
        
        public static List<String> splitSQLScript(List<String> script) {
            
            String DEFAULT_DELIMITER = ";";
            String PL_SQL_DELIMITER = "@@";
            boolean inPlsqlBlock = false;
            
		List<String> queryBlocks = new ArrayList<String>();
		StringBuffer command = null;
                
                for (String line : script){
                    if (command == null) {
                        command = new StringBuffer();
                    }
                    String trimmedLine = line.trim();
                    if (trimmedLine.startsWith(PL_SQL_DELIMITER) || trimmedLine.endsWith(PL_SQL_DELIMITER)) {
                        if (inPlsqlBlock == false) {
            		   inPlsqlBlock = true;
                        } else {
                               inPlsqlBlock = false;
                        }
                    }
                    // Interpret SQL Comment & Some statement that are not executable
                    if (trimmedLine.startsWith("--")
                       || trimmedLine.startsWith("//")
                       || trimmedLine.startsWith("#")
                       || trimmedLine.toLowerCase().startsWith("rem inserting into")
                       || trimmedLine.toLowerCase().startsWith("set define off")) {
                        // do nothing...
                    } else if (inPlsqlBlock == true) {
                        command.append(line.replace(PL_SQL_DELIMITER, " "));
                    } else if (trimmedLine.endsWith(DEFAULT_DELIMITER) || trimmedLine.endsWith(PL_SQL_DELIMITER)) { // Line is end of statement
                        // Append
                        if (trimmedLine.endsWith(DEFAULT_DELIMITER)) {
                            command.append(line.substring(0, line.lastIndexOf(DEFAULT_DELIMITER)));
                            command.append(";");
                        } else if (trimmedLine.endsWith(PL_SQL_DELIMITER)) {
                            command.append(line.substring(0, line.lastIndexOf(PL_SQL_DELIMITER)));
                            command.append(" ");
                        }
                        queryBlocks = addToBlock(command.toString(), queryBlocks);
                        command = null;
                    } else { // Line is middle of a statement
                   // Append
                        command.append(line);
                        command.append(" ");
                    }
                }
                return queryBlocks;
        }
	
	private static List<String> addToList(String query, List<String> instructions){
		String queryTrim = query.trim();
		if (!StringUtils.isBlank(queryTrim) && !queryTrim.equals(";")){
			instructions.add(queryTrim.trim());
		}
		return instructions;
	}
        
        private static List<String> addToBlock(String query, List<String> instructions){
		instructions.add(query);
		return instructions;
	}
        
}
