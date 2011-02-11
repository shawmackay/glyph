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
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.lookup.JoinManager;

/**
 * 
 * @author calum
 */
public class ServiceBuilderImpl implements ServiceBuilder {

    private String[] myGroups;

    private LookupLocator[] myLocators;

    private Entry[] myAttributes;

    private String myExportTypeName;

    private String exporterDirectory;

    /** Creates a new instance of ServiceBuilderImpl */
    public ServiceBuilderImpl() {
    }

    public ServiceBuilderData initService(Remote serviceObject, Configuration config, String componentName, ActivationID actID) {
        try {

            intialiseDefaults(serviceObject, config, componentName);

            Uuid myUuid = UuidFactory.generate();
            ProxyPair exportPair = DefaultExporterManager.getManager("default").exportProxyAndPair(serviceObject, myExportTypeName, myUuid);

            ServiceID svcId = handleServiceID(config, componentName, actID);

            ServiceBuilderData svcData = new ServiceBuilderData();
            LookupDiscoveryManager ldm = new LookupDiscoveryManager(myGroups, myLocators, null, config);
            loadAttributesBuilder(serviceObject);
            JoinManager jm = new JoinManager((Remote) exportPair.getSmartProxy(), myAttributes, svcId, ldm, null, config);
            
            setupServiceData(myUuid, exportPair, svcData, ldm, jm);
            return svcData;
        } catch (ExportException ex) {
            ex.printStackTrace();
        } catch (ConfigurationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void setupServiceData(Uuid myUuid, ProxyPair exportPair, ServiceBuilderData svcData, LookupDiscoveryManager ldm, JoinManager jm) {
        svcData.setLookupDiscoveryManager(ldm);
        svcData.setJoinManager(jm);
        svcData.setUuid(myUuid);
        if(exportPair!=null){
        svcData.setServiceProxyObject(exportPair.getSmartProxy());
        svcData.setRemoteProxyObject(exportPair.getRemoteExportedProxy());
        } 
    }

    private void loadAttributesBuilder(Object serviceObject) {
        String className = serviceObject.getClass().getName();
        if (className.endsWith("ServiceImpl")) {
            try {
                className = className.replace("ServiceImpl", "");
                Class cl = Class.forName(className + "AttributeBuilder");
                AttributeBuilder builder = (AttributeBuilder) cl.newInstance();
                myAttributes = builder.buildAttributes(myAttributes);
            } catch (ClassNotFoundException e) {
                // Let this exception past....
                // ....if we can't find the class then we didn't have to
                // create the attribute builder at generation time
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private ServiceID handleServiceID(Configuration config, String componentName, ActivationID actID) throws ConfigurationException, IOException, FileNotFoundException {
        ServiceID svcId = null;
        String storeDirectory = null;
        try {
            if (actID != null) {

                storeDirectory = (String) config.getEntry(componentName, "persistentDirectory", String.class);
                File svcIdFile = new File(new File(storeDirectory), componentName + ".svcid");
                if (svcIdFile.exists()) {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(svcIdFile));
                    Object savedSvcId = ois.readObject();
                    ois.close();
                    svcId = (ServiceID) savedSvcId;
                } else {

                    File storeDir = new File(storeDirectory);
                    if (!storeDir.exists())
                        ;
                    storeDir.mkdirs();
                    svcId = null;
                }
            }
            // generate a new lookup ID
            if (svcId == null) {

                Uuid serviceID = UuidFactory.generate();
                svcId = new ServiceID(serviceID.getMostSignificantBits(), serviceID.getLeastSignificantBits());

                if (storeDirectory != null) {
                    File svcIdFile = new File(new File(storeDirectory), componentName + ".svcid");
                    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(svcIdFile)));
                    oos.writeObject(svcId);
                    oos.flush();
                    oos.close();
                }

            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return svcId;
    }

    private void intialiseDefaults(Object serviceObject, Configuration config, String componentName) throws ConfigurationException {
        String[] myDefaultGroups = LookupDiscovery.ALL_GROUPS;
        LookupLocator[] myDefaultLocators = null;
        Entry[] myDefaultAttributes = null;
        String myDefaultExportTypeName = "default";
        myGroups = (String[]) config.getEntry(componentName, "initialGroups", String[].class, myDefaultGroups);
        myLocators = (LookupLocator[]) config.getEntry(componentName, "initialLocators", String[].class, myDefaultLocators);
        myAttributes = (Entry[]) config.getEntry(componentName, "initialAttributes", Entry[].class, myDefaultAttributes);
        myExportTypeName = (String) config.getEntry(componentName, "exporterTypeName", String.class, serviceObject.getClass().getName());
        exporterDirectory = (String) config.getEntry(componentName, "exporterDefDir", String.class, "conf/expdir");
        DefaultExporterManager.loadManagersFromDir(exporterDirectory);
    }

    public ServiceBuilderData initLocalService(Serializable serviceObject, Configuration config, String componentName, ActivationID actID) {
        try {

            intialiseDefaults(serviceObject, config, componentName);

            Uuid myUuid = UuidFactory.generate();
            //ProxyPair exportPair = DefaultExporterManager.getManager("default").exportProxyAndPair(serviceObject, myExportTypeName, myUuid);

            ServiceID svcId = handleServiceID(config, componentName, actID);
            
            ServiceBuilderData svcData = new ServiceBuilderData();
            LookupDiscoveryManager ldm = new LookupDiscoveryManager(myGroups, myLocators, null, config);
            loadAttributesBuilder(serviceObject);
            ProxyPair serializedPair = new ProxyPair(serviceObject, serviceObject);
            JoinManager jm = new JoinManager(serviceObject, myAttributes, svcId, ldm, null, config);
            //Check if we're using an adapter
           
            System.out.println("Run JoinManager");
            setupServiceData(myUuid, serializedPair, svcData, ldm, jm);
            return svcData;
        } catch (ExportException ex) {
            ex.printStackTrace();
        } catch (ConfigurationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
