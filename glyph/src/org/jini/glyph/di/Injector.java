/*
 * neon : org.jini.projects.neon.di
 * 
 * 
 * ConfigScanner.java
 * Created on 23-Aug-2005
 * 
 * ConfigScanner
 *
 */

package org.jini.glyph.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

/**
 * @author calum
 */
public class Injector {

	// public String filename;

	Configuration config;

	private ConfigurationFileScan scan;

	private Map<String, Map<String, Method>> classMethodMap = null;// new

	private Map<String, DIPlugin> lookupPlugins;

	// HashMap<String,
	// Map<String,
	// Method>>();

	public Injector(ConfigurationFileScan scan, Configuration config, Map<String, Map<String, Method>> classMethodMap) {
		this.config = config;
		this.scan = scan;
		this.classMethodMap = classMethodMap;
	}

	public void setLookupPlugins(Map<String, DIPlugin> lookupPlugins) {
		//System.out.println("Setting Lookup Plugins to: " + lookupPlugins);
		this.lookupPlugins = lookupPlugins;
	}

	public void inject(Object toSet) {
		inject(toSet, null);
	}

	public Object create(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class cl = Class.forName(className);
		return create(cl);
	}

	public Object create(Class cl) throws IllegalAccessException, InstantiationException {
		Object o = cl.newInstance();
		inject(o);
		return o;
	}

	public Object create(String className, String refID) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class cl = Class.forName(className);
		return create(cl, refID);
	}

	public Object create(String className, Class[] constructorTypes) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
		Class cl = Class.forName(className);
		return create(cl, constructorTypes);
	}

	public Object create(Class cl, Class[] constructorTypes) throws IllegalAccessException, InstantiationException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {

		return create(cl, constructorTypes, null);
	}

	public Object create(String className, Class[] constructorArgs, String refID) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
		Class cl = Class.forName(className);
		return create(cl, constructorArgs, refID);
	}

	public Object create(Class cl, Class[] constructorTypes, String refID) throws IllegalAccessException, InstantiationException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

		String compName = getComponentName(cl.getClass().getName(), refID);
		try {

			Object[] constructorArgs = (Object[]) config.getEntry(compName, "constructor", Object[].class);
			Constructor construct = cl.getConstructor(constructorTypes);
			construct.newInstance(constructorArgs);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Object o = null;
		inject(o, refID);
		return o;
	}

	public Object create(Class cl, String refID) throws IllegalAccessException, InstantiationException {
		Object o = cl.newInstance();
		inject(o, refID);
		return o;
	}

	public void inject(Object toSet, String refID) {
		long start = System.currentTimeMillis();
		//System.out.println("Injecting: " + toSet.getClass().getName());
		try {
			Map<String, Method> methMap;
			Class cl = toSet.getClass();
			if (classMethodMap.containsKey(cl.getName())) {
				methMap = classMethodMap.get(cl.getName());
			} else {
				Method[] methods = cl.getMethods();
				methMap = new HashMap<String, Method>();
				for (int i = 0; i < methods.length; i++) {
					methMap.put(methods[i].getName(), methods[i]);
				}
				classMethodMap.put(cl.getName(), methMap);
			}
			String componentName = null;

			ConfigurationComponent component;
			componentName = getComponentName(toSet.getClass().getName(), refID);
			//System.out.println("Looking for component: " + componentName);

			component = scan.getComponent(componentName);
			if (component == null) {
				if (componentName.endsWith("Impl")) {
					component = scan.getComponent(componentName.replace("Impl", ""));
					if (component != null)
						componentName = componentName.replace("Impl", "");
				}
				if (componentName.endsWith("Adapter")) {
					component = scan.getComponent(componentName.replace("Adapter", ""));
					if (component != null)
						componentName = componentName.replace("Adapter", "");
				}
			}

			HashMap entries = component.getEntries();

			for (Object o : entries.values()) {
				ConfigurationEntry entry = (ConfigurationEntry) o;
				String var = entry.getVariable();

				// System.out.println("Object class: " +
				// configObject.getClass().getName());
				StringBuffer methodName = new StringBuffer("set" + var.substring(0, 1).toUpperCase() + var.substring(1));
				String methodNameStr = methodName.toString();

				if (methMap.containsKey(methodNameStr)) {
					try {

						Method m = (Method) methMap.get(methodNameStr);// cl.getMethod(methodName.toString(),
						// new
						// Class[]{configObject.getClass()});
						// System.out.println(m.getName());
						long configstart = System.currentTimeMillis();
						Object configObject = config.getEntry(componentName, var, Object.class);
						long configend = System.currentTimeMillis();
						// System.out.println("Time from
						// Configuration for " + var +":
						// " + (configend-configstart));

						if (lookupPlugins.containsKey(configObject.getClass().getName())) {
							DIPlugin plugin = lookupPlugins.get(configObject.getClass().getName());
							if (plugin != null) {
								Object pluginReturn = plugin.findReference(configObject);
								configObject = pluginReturn;
							}
						}
						if (configObject instanceof String) {
							String str = (String) configObject;
							if (str.indexOf("$") != -1) {
								String refComponentName = str.substring(0, str.lastIndexOf("$"));
								String ID = str.substring(str.lastIndexOf("$") + 1);
								// System.out.println("Finding Component [" +
								// refComponentName + "] with ID:" + ID);
								try {
									Class dependentClass = Class.forName(refComponentName);
									long cstart = System.currentTimeMillis();
									Object createdDependent = dependentClass.newInstance();
									long cend = System.currentTimeMillis();

									inject(createdDependent, ID);
									configObject = createdDependent;
								} catch (ClassNotFoundException e) {
									// TODO
									// Handle
									// ClassNotFoundException
									e.printStackTrace();
								} catch (InstantiationException e) {
									// TODO
									// Handle
									// InstantiationException
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									// TODO
									// Handle
									// IllegalAccessException
									e.printStackTrace();
								}
							}

						}
						m.invoke(toSet, new Object[] { configObject });
					} catch (SecurityException e) {
						// TODO Handle SecurityException
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Handle
						// IllegalArgumentException
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Handle
						// IllegalAccessException
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Handle
						// InvocationTargetException
						e.printStackTrace();
					}
				}
			}

		} catch (ConfigurationException e) {
			// TODO Handle ConfigurationException
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		// System.out.println("Handling of object [" +
		// toSet.getClass().getName() + "$" + refID+ "] took " +
		// (end-start) + " ms");
	}

	private String getComponentName(String className, String refID) {
		String componentName;
		if (refID == null) {
			componentName = className;
		} else {
			componentName = className + "$" + refID;
		}
		return componentName;
	}

}
