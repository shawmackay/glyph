/*
 * ServiceBuilderImpl.java
 *
 * Created on 20 September 2006, 13:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jini.glyph;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.activation.ActivationID;
import java.rmi.server.ExportException;

import org.jini.glyph.chalice.DefaultExporterManager;
import org.jini.glyph.chalice.ProxyPair;
import org.jini.glyph.di.DIFactory;
import org.jini.glyph.di.Injector;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;

/**
 * 
 * @author calum
 */
public class ClientBuilderImpl implements ClientBuilder, ServiceDiscoveryListener {

    private String[] myGroups;

    private LookupLocator[] myLocators;

    private Entry[] myAttributes;

    private String myExportTypeName;

    private Class[] myServiceClass;

    private ServiceItemFilter myFilter;

    private LookupCache cache;

    /** Creates a new instance of ServiceBuilderImpl */
    public ClientBuilderImpl() {
    }

    public void  initClient(Configuration config, String componentName) {
        try {

            ClientBuilderData cbd = intialiseDefaults(config, componentName);
            LookupDiscoveryManager ldm = new LookupDiscoveryManager(myGroups, myLocators, null, config);
            LeaseRenewalManager lrm = new LeaseRenewalManager(config);
            ServiceDiscoveryManager sdm = new ServiceDiscoveryManager(ldm, lrm, config);
            ServiceTemplate templ = new ServiceTemplate(null, cbd.getLookupClasses(), cbd.getLookupAttributes());
            cache = sdm.createLookupCache(templ, cbd.getFilter(), null);

        } catch (ExportException ex) {
            ex.printStackTrace();
        } catch (ConfigurationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public ServiceItem getService(ServiceItemFilter filter){
        return cache.lookup(filter);
    }

    private ClientBuilderData intialiseDefaults(Configuration config, String componentName) throws ConfigurationException {
        String[] myDefaultGroups = LookupDiscovery.ALL_GROUPS;
        LookupLocator[] myDefaultLocators = null;
        Entry[] myDefaultAttributes = null;
        String myDefaultExportTypeName = "default";
        myGroups = (String[]) config.getEntry(componentName, "lookupGroups", String[].class, myDefaultGroups);
        myLocators = (LookupLocator[]) config.getEntry(componentName, "lookupLocators", String[].class, myDefaultLocators);
        myAttributes = (Entry[]) config.getEntry(componentName, "lookupAttributes", Entry[].class, myDefaultAttributes);
        myServiceClass = (Class[]) config.getEntry(componentName, "lookupServiceClass", Class[].class, null);
        myFilter = (ServiceItemFilter) config.getEntry(componentName, "lookupFilter", ServiceItemFilter.class,null);
        ClientBuilderData data = new ClientBuilderData();
        data.setFilter(myFilter);
        data.setLookupAttributes(myAttributes);
        data.setLookupClasses(myServiceClass);
        return data;
    }

    public void serviceAdded(ServiceDiscoveryEvent event) {
        // TODO Auto-generated method stub
        System.out.println("Adding: " + event.getPostEventServiceItem().service.getClass().getName());
    }

    public void serviceChanged(ServiceDiscoveryEvent event) {
        // TODO Auto-generated method stub
        
    }

    public void serviceRemoved(ServiceDiscoveryEvent event) {
        // TODO Auto-generated method stub
        
    }

}
