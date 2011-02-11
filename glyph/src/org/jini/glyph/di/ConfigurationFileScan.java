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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @ @author Calum
 *  
 */
public class ConfigurationFileScan implements Serializable{
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3617287947921995574L;
	private ArrayList imports = new ArrayList();
	private ArrayList includes = new ArrayList();
	private HashMap components = new HashMap();
	private String filename;

	public ConfigurationFileScan(String filename) {
		this.filename = filename;
	}

	public String[] getImports() {
		return (String[]) imports.toArray(new String[]{});
	}
	
	public List getImportsSet(){
		return imports;
	}
	
	public String[] getIncludes() {
		return (String[]) includes.toArray(new String[]{});
	}
	
	public List getIncludesSet() {
		return includes;
	}

	public void addComponent(ConfigurationComponent component) {
		components.put(component.getComponentName(), component);
	}

	public ConfigurationComponent getComponent(String name) {
		return (ConfigurationComponent) components.get(name);
	}

	/**
	 * @return Returns the components.
	 */
	public HashMap getComponents() {
		return components;
	}

	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}

	public void addImport(String importname) {
		imports.add(importname);
	}

	public void removeImport(String importname) {
		imports.remove(importname);
	}

	public void addInclude(String includeName) {
		includes.add(includeName);
	}

	public void removeInclude(String includeName) {
		includes.remove(includeName);
	}
	
	public void removeComponent(String componentName){
	    components.remove(componentName);
	}
	
	public String reconstitute() {
		StringBuffer out = new StringBuffer();
		for (Iterator iter = imports.iterator(); iter.hasNext();) {
			String importname = (String) iter.next();
			out.append("import " + importname + ";\n");
		}
		out.append("\n\n");
		
		for (Iterator iter = includes.iterator(); iter.hasNext();) {
			String importname = (String) iter.next();
			out.append("/*#include '" + importname + "';*/\n");
		}
		out.append("\n\n");
		for (Iterator iter = components.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();

			out.append((String) entry.getKey() + "{\n");
			reconstituteComponent((ConfigurationComponent) entry.getValue(), out);
			out.append("}\n\n");
		}
		return out.toString();
	}

	private void reconstituteComponent(ConfigurationComponent c, StringBuffer out) {
		String[] entryNames = c.getEntryNames();
		for (int i = 0; i < entryNames.length; i++) {
			ConfigurationEntry ent = c.getEntry(entryNames[i]);
			BufferedReader rdr = new BufferedReader(new StringReader(ent.getComment()));
			out.append("\t/*\n\t* Entry: " + ent.getVariable() + "\n");
			try {
				String commentLine = rdr.readLine();
				while (commentLine != null) {
					out.append("\t* " + commentLine + "\n");
					commentLine = rdr.readLine();
				}
			} catch (IOException e) {
				// TODO Handle IOException
				e.printStackTrace();
			}
			out.append("\t*/\n");
			String expression = ent.getAssignedexpression();
			if (expression != null) {
				out.append("\t" + ent.getVariable() + "=" + expression);
				if (!expression.endsWith(";"))
					out.append(";");
			} else
				out.append("\t" + ent.getVariable() + "= null;");
			out.append("\n\n");
		}
	}
	
	
}
