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
 * ClassFilter.java
 * Created on 22-Jul-2003
 */
package org.jini.glyph.filters;

import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.ServiceItemFilter;

/**
 * @author calum
 */
public class ClassFilter implements ServiceItemFilter {
	
	Class cl;
	public ClassFilter(Class cl) {
		this.cl = cl;
		
	}
	public boolean check(ServiceItem item) {
		//System.out.println("Checking " + cl.getName() + " against " + item.service.getClass().getName());
		
		if (cl.isInstance(item.service) )
			return true;
		else
			return false;
	}
}