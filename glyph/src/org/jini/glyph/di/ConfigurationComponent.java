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
import java.util.HashMap;

/**
 * @
 * @author Calum
 * 
 */
public class ConfigurationComponent implements Serializable{
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3258407344076699960L;
	private String componentName;
	private HashMap entries = new HashMap();
	public ConfigurationComponent(String componentName){
		this.componentName = componentName;
	}
	
	public void addEntry(ConfigurationEntry entry){
		entries.put(entry.getVariable(),entry);
	}
	
	public ConfigurationEntry getEntry(String name){
		return (ConfigurationEntry) entries.get(name);
	}
	
	public void removeEntry(String name){
		entries.remove(name);
	}
	
	public String[] getEntryNames(){
		return (String[]) entries.keySet().toArray(new String[]{} );
	}
	/**
	 * @return Returns the componentName.
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * @return Returns the entries.
	 */
	public HashMap getEntries() {
		return entries;
	}

}
