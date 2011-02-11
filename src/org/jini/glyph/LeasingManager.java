package org.jini.glyph;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;

public class LeasingManager {

	private static Map<String, com.sun.jini.landlord.Landlord> landlords = new TreeMap<String, com.sun.jini.landlord.Landlord>();

	private Logger log = Logger.getLogger("org.jini.glyph");
	
	/**
	 * Adds the object <i>o</i> as a leased resource to a landlords' managed set.
	 * This method will create a landlord, use a <code>LeaseHelper</code> derived from the classname
	 * of 'o' and apply a configuration to it.<br/>
	 * The landlord is only created once and therefore calling this method with a configuration more than once ill have no effect
	 * 
	 * @param o The leased resource object
	 * @param requiredDuration The duration requested for ths lease
	 * @param config The configuration to initialise the landlord with
	 * @return the Lease for the object
	 * @throws UnsupportedOperationException
	 * @throws LeaseDeniedException
	 */
	public Lease addLeasedResource(Object o, long requiredDuration,Configuration config)
			throws UnsupportedOperationException, LeaseDeniedException {

		String className = o.getClass().getName();
		Object landlord = null;
		if (!landlords.containsKey(className)) {
			try {
				log.info("Creating new Landlord now");
				Class landLordClass = Class.forName(className
						+ "Landlord");
				Class leaseHelper = Class.forName(className + "LeaseHelper");
				Object helper = leaseHelper.newInstance();
				Constructor c = landLordClass
						.getConstructor(LandlordHelper.class, Configuration.class);
				
				landlord = c.newInstance(helper,config);
				landlords.put(className, (com.sun.jini.landlord.Landlord) landlord);

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		landlord = landlords.get(className);
		try {
			Method newLease = landlord.getClass().getMethod("newLease",
					new Class[] { o.getClass(), long.class });
			Object lease = newLease.invoke(landlord, o, requiredDuration);
			System.out.println("Lease is of type: " + lease);
			return (Lease) lease;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Adds the object <i>o</i> as a leased resource to a landlords' managed set.
	 * This method will create a landlord, use a <code>LeaseHelper</code> derived from the classname
	 * of 'o' and apply a configuration to it.<br/>
	 * The landlord is only created once and therefore calling this method with a configuration more than once ill have no effect
	 * 
	 * @param o The leased resource object
	 * @param requiredDuration The duration requested for ths lease
	 * @param config The configuration to initialise the landlord with
	 * @param helper The LandlordHelper that is used to deallocate resources once the Lease has been reaped to initialise the landlord with
	 * @return The reuired Lease
	 * @throws UnsupportedOperationException
	 * @throws LeaseDeniedException
	 */
	
	public Lease addLeasedResource(Object o, long requiredDuration,
			Configuration config,LandlordHelper helper) throws UnsupportedOperationException,
			LeaseDeniedException {
		String className = o.getClass().getName();
		Object landlord = null;
		if (!landlords.containsKey(className)) {
			try {
				log.info("Creating new Landlord now");
				Class landLordClass = Class.forName(className
						+ "LandlordWrapper");

				Constructor c = landLordClass
						.getConstructor(LandlordHelper.class);
				landlord = c.newInstance(helper);
				log.info("Linking Landlord to existing LandlordHelper");
				landlords.put(className, (com.sun.jini.landlord.Landlord) landlord);

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		landlord = landlords.get(className);
		try {
			Method newLease = landlord.getClass().getMethod("newLease",
					new Class[] { o.getClass(), long.class });
			Object lease = newLease.invoke(landlord, o, requiredDuration);
			System.out.println("Lease is of type: " + lease);
			return (Lease) lease;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Adds the object <i>o</i> as a leased resource to a landlords' managed set.
	 * This method will create a landlord, use a <code>LeaseHelper</code> derived from the classname
	 * of 'o' and apply a configuration to it.<br/>
	 * The landlord is only created once and therefore calling this method with a configuration more than once ill have no effect
	 * 
	 * @param o The leased resource object
	 * @param requiredDuration The duration requested for ths lease
	 * @param helper The LandlordHelper that is used to deallocate resources once the Lease has been reaped to initialise the landlord with
	 * @return The reuired Lease
	 * @throws UnsupportedOperationException
	 * @throws LeaseDeniedException
	 */
	
	public Lease addLeasedResource(Object o, long requiredDuration,
			LandlordHelper helper) throws UnsupportedOperationException,
			LeaseDeniedException {
		String className = o.getClass().getName();
		Object landlord = null;
		if (!landlords.containsKey(className)) {
			try {
				log.info("Creating new Landlord now");
				Class landLordClass = Class.forName(className
						+ "Landlord");

				Constructor c = landLordClass
						.getConstructor(LandlordHelper.class, Configuration.class);
				Configuration config = ConfigurationProvider.getInstance(new String[]{"conf/"+className.substring(className.lastIndexOf(".")+1)+"-landlord.config"});
				landlord = c.newInstance(helper, config);
				log.info("Linking Landlord to existing LandlordHelper");
				landlords.put(className, (com.sun.jini.landlord.Landlord) landlord);

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		landlord = landlords.get(className);
		try {
			Method newLease = landlord.getClass().getMethod("newLease",
					new Class[] { o.getClass(), long.class });
			Object lease = newLease.invoke(landlord, o, requiredDuration);
			System.out.println("Lease is of type: " + lease);
			return (Lease) lease;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
