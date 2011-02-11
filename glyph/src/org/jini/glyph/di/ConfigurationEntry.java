/*
   Copyright 2006 thor.jini.org Project

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/*
 * Created on 24-Jul-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jini.glyph.di;

import java.io.Serializable;

/**
 * @
 * @author Calum
 * 
 */
public class ConfigurationEntry implements Serializable{
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3977857371680749622L;
	private String comment;
	private String variable;
	private String assignedexpression;
    private boolean isStatic;
    
	/**
	 * @return Returns the assignedexpression.
	 */
	public String getAssignedexpression() {
		return assignedexpression;
	}

	/**
	 * @param assignedexpression The assignedexpression to set.
	 */
	public void setAssignedexpression(String assignedexpression) {
		this.assignedexpression = assignedexpression;
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return Returns the variable.
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * @param variable The variable to set.
	 */
	public void setVariable(String variable) {
		this.variable = variable;
	}

	/**
	 * @param comment
	 * @param variable
	 * @param assignedexpression
	 */
	public ConfigurationEntry(boolean isStatic, String comment, String variable, String assignedexpression) {
		super();
        this.isStatic = isStatic;
		this.comment = comment;
		this.variable = variable;
		this.assignedexpression = assignedexpression;
		
//		System.out.println("ConfigEntry:");
//		System.out.println("\tComment:\t " +this.comment);
//		System.out.println("\tVariable :  \t " +this.variable);
//		System.out.println("\tassigned:\t " +this.assignedexpression);
	}
	
	public String toString(){
		return assignedexpression;
	}
	
	public ConfigurationEntry(){
		
	}

        public boolean isStatic() {
                return isStatic;
        }

        public void setStatic(boolean isStatic) {
                this.isStatic = isStatic;
        }
	
}
