/*
* Copyright 2005 neon.jini.org project 
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License. 
* You may obtain a copy of the License at 
* 
*       http://www.apache.org/licenses/LICENSE-2.0 
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

/*
 * NameFilter.java
 * Created on 22-Jul-2003
 */
package org.jini.glyph.filters;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.entry.Name;

/**
 * @author calum
 */
public class NameFilter extends ClassFilter {
	
	String name;

	public NameFilter(String name, Class cl) {
		super(cl);
		
		this.name = name;
	}
	/**
	 * @see net.jini.lookup.ServiceItemFilter#check(net.jini.core.lookup.ServiceItem)
	 */
	public boolean check(ServiceItem item) {
		if (!super.check(item))
			return false;
		Entry[] attr = item.attributeSets;
		for (int i = 0; i < attr.length; i++)
			if (attr[i] instanceof Name) {
				Name n = (Name) attr[i];
				//System.out.print("\t Checking Name: [" + this.name +"] against [" + n.name + "]");
				if (n.name.equals(this.name)){
					//System.out.println("..yes");
					return true;
				}
				//else
				
					//System.out.println("...no");
			}
		return false;
	}
}