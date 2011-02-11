// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   DefaultExporterManager.java

package org.jini.glyph.chalice;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.util.*;
import java.util.logging.*;

import org.jini.glyph.chalice.builder.ProxyCreator;
import org.jini.glyph.test.SimpleCreator;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.export.Exporter;
import net.jini.id.Uuid;
import net.jini.jeri.*;
import net.jini.jeri.http.HttpServerEndpoint;
import net.jini.jeri.tcp.TcpServerEndpoint;

// Referenced classes of package utilities20.export:
//            ExporterManager, LogFormatter, ExporterDefinition, ExpProxyHolder,
//            ExporterHolder, ReferentialDefinition

public class DefaultExporterManager implements ExporterManager {
    private class ExporterReaper implements Runnable {

        public void run() {
            do {
                ArrayList reapList = new ArrayList();
                for (int i = 0; i < scheduleWorkList.size(); i++) {
                    RelinquishJob job = (RelinquishJob) scheduleWorkList.get(i);
                    if (job.getRelinquishTime() < System.currentTimeMillis()) {
                        l.finer("Reaper removing exporter for: " + job.exporterName);
                        relinquish(job.getExporterName(), job.getExporterID());
                        reapList.add(job);
                    }
                }

                for (int i = 0; i < reapList.size(); i++)
                    synchronized (scheduleWorkList) {
                        if (scheduleWorkList.remove(reapList.get(i)))
                            l.finest("Relinquish Job removed");
                        else
                            l.finest("Relinquish Job not removed");
                    }

                try {
                    Thread.sleep(5000L);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } while (true);
        }

        ExporterReaper() {
        }
    }

    private class RelinquishJob {

        public Uuid getExporterID() {
            return exporterID;
        }

        public String getExporterName() {
            return exporterName;
        }

        public long getRelinquishTime() {
            return relinquishTime;
        }

        private String exporterName;

        private Uuid exporterID;

        private long relinquishTime;

        public RelinquishJob(String exporterName, Uuid exporterID, long delay) {
            this.exporterName = exporterName;
            this.exporterID = exporterID;
            relinquishTime = System.currentTimeMillis() + delay;
        }
    }

    /**
     * @deprecated Method getManager is deprecated
     */

    public static ExporterManager getManager(String name, String filename) {
        if (!exportManagers.containsKey(name)) {
            // System.out.println("Creating Manager: " + name);
            ExporterManager mgr = new DefaultExporterManager(name, filename);
            exportManagers.put(name, mgr);
        }
        return (ExporterManager) exportManagers.get(name);
    }

    /**
     * Loads a manager using a configuration loaded from the specified filename,
     * using the given name.
     * 
     * @param name
     * @param config
     * @return
     */
    public static ExporterManager loadManager(String name, String filename) {
        if (!exportManagers.containsKey(name)) {
            System.out.println("Creating Manager: " + name);
            ExporterManager mgr = new DefaultExporterManager(name, filename);
            exportManagers.put(name, mgr);
        }
        return (ExporterManager) exportManagers.get(name);
    }

    /**
     * Loads multiple managers using the directory method<br/> Within the given
     * directory there are subdirectories, the name of which are the names of
     * the managers, within each of these subdirectories will be individual
     * configuarition files which are then parse and placed within the new
     * manager<br/> i.e. For the directory 'expdir'
     * 
     * <pre>
     * \---expdir
     *      \---default                                 &lt;--Manager name
     *      HelloImpl.config    &lt;-Configuration file
     *      HelloImplAdminImpl.config
     * </pre>
     * 
     * @param filename
     */
    public static void loadManagersFromDir(String filename) {

        File dirname = new File(filename);
        if (dirname.exists()) {
            File[] managernamedirs = dirname.listFiles();
            for (int i = 0; i < managernamedirs.length; i++) {
                File managername = managernamedirs[i];
                ExporterManager mgr = DefaultExporterManager.getManager(managername.getName());
                if(mgr == null){
                		mgr = new DefaultExporterManager(managername.getName(), managername);
                		exportManagers.put(managername.getName(), mgr);
                } else {
                		((DefaultExporterManager)mgr).mergeWithDir(managername.getAbsolutePath());
                }
            }
        }
    }

    public void mergeWithDir(String filename){
	   loadDirectoryExporterDefinitions(new File(filename));
    }
    
    
    /**
     * Loads a manager using the specified configuration object, using the given
     * name.
     * 
     * @param name
     * @param config
     * @return
     */
    public static ExporterManager loadManager(String name, Configuration config) {
        if (!exportManagers.containsKey(name)) {
            // l.fine("Creating Manager: " + name);
            ExporterManager mgr = new DefaultExporterManager(name, config);
            exportManagers.put(name, mgr);
        }
        return (ExporterManager) exportManagers.get(name);
    }

    /**
     * Obtains the default Exporter Manager<br/> The same as
     * <code>getManager("default")</code>
     * 
     * @return the default Exporter Manager or null
     */
    public static ExporterManager getManager() {
        return (ExporterManager) exportManagers.get("default");
    }

    /**
     * Obtains the given ExporterManager or null if it doesn't exist.
     * 
     * @param name
     *            name of the ExporterMangaer you want
     * @return the Exporter Manager or null
     */
    public static ExporterManager getManager(String name) {
        
        return (ExporterManager) exportManagers.get(name);
    }

    private DefaultExporterManager(String name, File theDirectory) {
        // System.out.println("Loading mgr [" + name + "] from " + theDirectory
        // .getAbsolutePath());
        scheduleWorkList = new ArrayList();
        l = Logger.getLogger("exportermanager" + name);
        l.setLevel(Level.FINEST);
        this.name = name;
        
        proxies = new HashMap();
        holders = new HashMap();
       
        
        loadDirectoryExporterDefinitions(theDirectory);
        System.out.println("Adding default exporter definition");
        addNamedExporter("default", TcpServerEndpoint.getInstance(0), new BasicILFactory(), null);
    }

	private void loadDirectoryExporterDefinitions(File theDirectory) {
		File[] definitions = theDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                // TODO Auto-generated method stub
                return pathname.getAbsolutePath().endsWith(".config");
            }
        });
        try {
            for (int i = 0; i < definitions.length; i++) {
                String defname = definitions[i].getName().split("\\.")[0];
                // System.out.println("DefName: " + defname);
                Configuration config = ConfigurationProvider.getInstance(new String[] { definitions[i].getAbsolutePath() });
                String definitionNames[] = (String[]) config.getEntry("ExportManager", "mgrDefs", java.lang.String[].class, new String[] { "" });
                for (int j = 0; j < definitionNames.length; j++) {

                    l.finest("Loading defname: " + definitionNames[j]);
                    Class exp = (Class) config.getEntry("ExportManager." + definitionNames[j], "exporterClass", java.lang.Class.class);

                    ServerEndpoint se = (ServerEndpoint) config.getEntry("ExportManager." + definitionNames[j], "serverEndpoint", net.jini.jeri.ServerEndpoint.class, HttpServerEndpoint.getInstance(0));
                    InvocationLayerFactory Ilf = (InvocationLayerFactory) config.getEntry("ExportManager." + definitionNames[j], "ILFactory", net.jini.jeri.InvocationLayerFactory.class, new BasicILFactory());
                    ProxyCreator pc = (ProxyCreator) config.getEntry("ExportManager." + definitionNames[j], "proxyCreator", org.jini.glyph.chalice.builder.ProxyCreator.class, new SimpleCreator());
                    String referentialLoader = (String) config.getEntry("ExportManager." + definitionNames[j], "referentialInstance", java.lang.String.class, null);
                    if (referentialLoader == null) {
                        // System.out.println("Loaded exporter [" + defname + "]
                        // from " + definitions[i].getAbsolutePath());
                        addNamedExporter(definitionNames[j].toLowerCase(), se, Ilf, pc, exp);
                    } else {
                        // l.info("Building referential Exporter");
                        addReferentialExporter(definitionNames[j], referentialLoader, pc);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}

    private DefaultExporterManager(String name, String filename) {
        scheduleWorkList = new ArrayList();
        l = Logger.getLogger("exportermanager" + name);
        l.setLevel(Level.FINEST);
        this.name = name;
        try {
            proxies = new HashMap();
            holders = new HashMap();
            l.finest("Creating definitions from: [" + filename + "]");
            if (filename != null) {
                config = ConfigurationProvider.getInstance(new String[] { filename });
                String definitionNames[] = (String[]) config.getEntry("ExportManager", "mgrDefs", java.lang.String[].class, new String[] { "" });
                String logLevel = (String) config.getEntry("ExportManager", "logLevel", java.lang.String.class, "INFO");
                l.setLevel(Level.parse(logLevel));
                l.setUseParentHandlers(false);
                ConsoleHandler logHandler = new ConsoleHandler();
                logHandler.setFormatter(new LogFormatter());
                logHandler.setLevel(Level.parse(logLevel));
                l.addHandler(logHandler);
                for (int i = 0; i < definitionNames.length; i++) {
                    l.info("Defining [" + definitionNames[i] + "] export rule for ExporterManager " + name);
                    Class exp = (Class) config.getEntry("ExportManager." + definitionNames[i], "exporterClass", java.lang.Class.class, net.jini.jeri.BasicJeriExporter.class);
                    ServerEndpoint se = (ServerEndpoint) config.getEntry("ExportManager." + definitionNames[i], "serverEndpoint", net.jini.jeri.ServerEndpoint.class, HttpServerEndpoint.getInstance(0));
                    InvocationLayerFactory Ilf = (InvocationLayerFactory) config.getEntry("ExportManager." + definitionNames[i], "ILFactory", net.jini.jeri.InvocationLayerFactory.class, new BasicILFactory());
                    ProxyCreator pc = (ProxyCreator) config.getEntry("ExportManager." + definitionNames[i], "proxyCreator", org.jini.glyph.chalice.builder.ProxyCreator.class, new SimpleCreator());
                    String referentialLoader = (String) config.getEntry("ExportManager." + definitionNames[i], "referentialInstance", java.lang.String.class, null);
                    if (referentialLoader == null) {
                        addNamedExporter(definitionNames[i], se, Ilf, pc, exp);
                    } else {
                        l.info("Building referential Exporter");
                        addReferentialExporter(definitionNames[i], referentialLoader, pc);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        reaperThread = new Thread(new ExporterReaper());
        reaperThread.start();
        System.out.println("Adding default exporter definition");
        addNamedExporter("default", TcpServerEndpoint.getInstance(0), new BasicILFactory(), null);
    }

    private DefaultExporterManager(String name, Configuration config) {
        scheduleWorkList = new ArrayList();
        l = Logger.getLogger("exportermanager");
        l.setLevel(Level.FINEST);
        this.name = name;
        try {
            proxies = new HashMap();
            holders = new HashMap();
            if (config != null) {
                String definitionNames[] = (String[]) config.getEntry("ExportManager", "mgrDefs", java.lang.String[].class, new String[] {});

                if (definitionNames.length == 0) {
                    String expdirectory = (String) config.getEntry("ExportManager", "mgrDir", String.class);

                } else {
                    boolean defaultExpFound = false;
                    for (int i = 0; i < definitionNames.length; i++) {
                        l.info("Defining " + definitionNames[i] + " export rule for ExporterManager " + name);
                        if (definitionNames[i].equals("Default"))
                            defaultExpFound = true;
                        Class exp = (Class) config.getEntry("ExportManager." + definitionNames[i], "exporterClass", java.lang.Class.class, net.jini.jeri.BasicJeriExporter.class);
                        ServerEndpoint se = (ServerEndpoint) config.getEntry("ExportManager." + definitionNames[i], "serverEndpoint", net.jini.jeri.ServerEndpoint.class, HttpServerEndpoint.getInstance(0));
                        InvocationLayerFactory Ilf = (InvocationLayerFactory) config.getEntry("ExportManager." + definitionNames[i], "ILFactory", net.jini.jeri.InvocationLayerFactory.class, new BasicILFactory());
                        ProxyCreator pc = (ProxyCreator) config.getEntry("ExportManager." + definitionNames[i], "proxyCreator", org.jini.glyph.chalice.builder.ProxyCreator.class, new SimpleCreator());
                        String referentialLoader = (String) config.getEntry("ExportManager." + definitionNames[i], "referentialInstance", java.lang.String.class, null);
                        if (referentialLoader == null) {
                            addNamedExporter(definitionNames[i].toLowerCase(), se, Ilf, pc, exp);
                        } else {
                            l.info("Building referential Exporter");
                            addReferentialExporter(definitionNames[i].toLowerCase(), referentialLoader, pc);
                        }
                    }
                }

                l.warning("No default definition available for exporter manager");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        reaperThread = new Thread(new ExporterReaper());
        reaperThread.start();
        System.out.println("Adding default exporter definition");
        addNamedExporter("default", TcpServerEndpoint.getInstance(0), new BasicILFactory(), null);
    }

    // public Remote exportObject(Object ob, String exporterTypeName, Uuid ID)
    // throws ExportException {
    // try {
    //
    // ArrayList combineProxies = new ArrayList();
    //
    // Class[] clazz = ob.getClass().getInterfaces();
    //
    // Class c = ob.getClass();
    //
    // String nonqualifiedClassName =
    // c.getName().substring(c.getName().lastIndexOf(".") + 1);
    // String packageName = c.getPackage().getName();
    // System.out.println("Looking for class: " + packageName + "." +
    // nonqualifiedClassName);
    // System.out.println("Base package Name: " + packageName);
    // Class remoteBindingClass = Class.forName(packageName + "." +
    // nonqualifiedClassName);
    // Constructor construct = null;
    // System.out.println("Class Loaded");
    // Constructor[] constructors = remoteBindingClass.getConstructors();
    // for (int i = 0; i < constructors.length; i++) {
    //
    // if (constructors[i].getParameterTypes().length == 1) {
    // Class paramType = constructors[i].getParameterTypes()[0];
    // System.out.println("Checking constructor param type: " +
    // paramType.getName() + " against " + ob.getClass());
    // if (paramType.isInstance(ob)) {
    // System.out.println("Found constructor!!!!!");
    // construct = constructors[i];
    // }
    // }
    // }
    // // remoteBindingClass.getConstructor(ob.getClass());
    // Object o;
    // System.out.println("Constructing Object");
    // if (construct != null)
    // o = construct.newInstance(ob);
    // else {
    // o = remoteBindingClass.newInstance();
    // System.out.println("Creating standard object");
    // }
    // Class creatorClass = Class.forName(packageName + ".constrainable." +
    // nonqualifiedClassName + "Creator");
    //	    
    // System.out.println("Object o = > " +o.getClass().getName());
    // ProxyCreator pc = (ProxyCreator) creatorClass.newInstance();
    // Remote returnableProxy = pc.create((Remote) o, ID);
    //	    
    // System.out.println("Looking for exporter: " + exporterTypeName);
    // System.out.println("Exporters:\n" + exporters);
    // ExporterDefinition exp = (ExporterDefinition)
    // exporters.get(exporterTypeName.toLowerCase());
    // Exporter exporter = exp.getExporter();
    // Remote exportable = exporter.export((Remote) o);
    // ExpProxyHolder instanceHolder = new ExpProxyHolder((Remote) o,
    // returnableProxy, exportable, ID, exporter);
    // ArrayList instancelist = null;
    // if (proxies.containsKey("default")) {
    // instancelist = (ArrayList) proxies.get("default");
    // } else {
    // instancelist = new ArrayList();
    // proxies.put("default", instancelist);
    // }
    // instancelist.add(instanceHolder);
    // holders.put(ID, instanceHolder);
    // Class[] backendIntf =returnableProxy.getClass().getInterfaces();
    // System.out.println("Interfaces of: " +
    // returnableProxy.getClass().getName());
    // for(int i=0;i<backendIntf.length;i++){
    // System.out.println("\t" + backendIntf[i].getClass().getName());
    // }
    // return returnableProxy;
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // throw new ExportException("Problem when finding export classes for
    // Object", ex);
    // }
    // }
    /**
     * Exports the given remote object, using the definition as specified by
     * <code>exporterName</code>. Returns the smart proxy (or the original
     * proxy if none is defined in the exporter definition).
     * 
     */
    public Remote exportProxy(Remote r, String exporterName, Uuid ID) throws ExportException {
        //System.out.println("Exporters is Null? " + (exporters == null) + ",exporterName: " + exporterName);
        if (exporters.containsKey(exporterName.toLowerCase())) {
            ExporterDefinition exp = (ExporterDefinition) exporters.get(exporterName.toLowerCase());
            if (exp == null) {
                exp = (ExporterDefinition) exporters.get("default");
                l.finer("Obtained default exporter");
            } else {
                l.fine("Obtained " + exporterName + " exporter");
            }
            if (exp != null) {
                l.fine("Exporting " + ID);
                Exporter exporter = exp.getExporter();
                Remote exportable = exporter.export(r);
                l.fine("Using creator: " + exp.getCreator().getClass().getName());
                Remote returnableProxy = exp.getCreator().create(exportable, ID);
                ExpProxyHolder instanceHolder = new ExpProxyHolder(r, returnableProxy, exportable, ID, exporter);
                ArrayList instancelist = null;
                if (proxies.containsKey(exporterName.toLowerCase())) {
                    instancelist = (ArrayList) proxies.get(exporterName.toLowerCase());
                } else {
                    instancelist = new ArrayList();
                    proxies.put(exporterName.toLowerCase(), instancelist);
                }
                instancelist.add(instanceHolder);
                holders.put(ID, instanceHolder);
                return returnableProxy;
            }
            l.info("Specified default exporter is not available to proxy for: " + exporterName);
        } else {
            l.warning("Specified exporter is not available: " + exporterName);
            RuntimeException ex = new RuntimeException();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Exports the given remote object, using the definition as specified by
     * <code>exporterName</code>. Returns a Proxy pair holding both the smart
     * proxy and remote proxy.
     * 
     */
    public ProxyPair exportProxyAndPair(Remote r, String exporterName, Uuid ID) throws ExportException {
        System.out.println("Exporters is Null? " + (exporters == null) + ",exporterName: " + exporterName);
        if (exporters.containsKey(exporterName.toLowerCase())) {
            ExporterDefinition exp = (ExporterDefinition) exporters.get(exporterName.toLowerCase());
            if (exp == null) {
                exp = (ExporterDefinition) exporters.get("default");
                l.finer("Obtained default exporter");
            } else {
                l.fine("Obtained " + exporterName + " exporter");
            }
            if (exp != null) {
                l.fine("Exporting " + ID);
                Exporter exporter = exp.getExporter();
                Remote exportable = exporter.export(r);
                l.fine("Using creator: " + exp.getCreator().getClass().getName());
                Remote returnableProxy = exp.getCreator().create(exportable, ID);
                ExpProxyHolder instanceHolder = new ExpProxyHolder(r, returnableProxy, exportable, ID, exporter);
                ArrayList instancelist = null;
                if (proxies.containsKey(exporterName.toLowerCase())) {
                    instancelist = (ArrayList) proxies.get(exporterName.toLowerCase());
                } else {
                    instancelist = new ArrayList();
                    proxies.put(exporterName.toLowerCase(), instancelist);
                }
                instancelist.add(instanceHolder);
                holders.put(ID, instanceHolder);

                return new ProxyPair(returnableProxy, exportable);
            }
         
        } else {
        	
        	 System.out.println("Exporting " + ID + " as non-smart proxy");
        	  ExporterDefinition exp = (ExporterDefinition) exporters.get("default");
              Exporter exporter = exp.getExporter();
              Remote exportable = exporter.export(r);
              ExpProxyHolder instanceHolder = new ExpProxyHolder(r, r, exportable, ID, exporter);
              ArrayList instancelist = null;
              if (proxies.containsKey(exporterName.toLowerCase())) {
                  instancelist = (ArrayList) proxies.get(exporterName.toLowerCase());
              } else {
                  instancelist = new ArrayList();
                  proxies.put(exporterName.toLowerCase(), instancelist);
              }
              instancelist.add(instanceHolder);
              holders.put(ID, instanceHolder);

              
            l.warning("Specified exporter is not available: " + exporterName);
            return new ProxyPair(exportable, exportable);
        	
        }
        return null;
    }

    public int getCount() {
        int count = 0;
        for (Iterator clearIter = proxies.entrySet().iterator(); clearIter.hasNext();) {
            java.util.Map.Entry ent = (java.util.Map.Entry) clearIter.next();
            List namedProxies = (List) ent.getValue();
            count += namedProxies.size();
        }

        return count;
    }

    public int getCount(String exporterName) {
        if (proxies.containsKey(exporterName.toLowerCase())) {
            List namedProxies = (List) proxies.get(exporterName.toLowerCase());
            return namedProxies.size();
        } else {
            return -1;
        }
    }

    public void addNamedExporter(String name, ServerEndpoint se, InvocationLayerFactory ilf, ProxyCreator creator) {
        if (exporters == null)
            exporters = new HashMap();
        exporters.put(name.toLowerCase(), new ExporterHolder(se, ilf, net.jini.jeri.BasicJeriExporter.class, creator));
    }

    public void addNamedExporter(String name, ServerEndpoint se, InvocationLayerFactory ilf, ProxyCreator creator, Class exporterClass) {
        if (exporters == null)
            exporters = new HashMap();
        exporters.put(name.toLowerCase(), new ExporterHolder(se, ilf, exporterClass, creator));
    }

    public void addReferentialExporter(String name, String reference, ProxyCreator creator) {
        if (exporters == null)
            exporters = new HashMap();
        exporters.put(name.toLowerCase(), new ReferentialDefinition(reference, creator));
    }

    public Collection getAllExportedUnder(String name) {
        return (Collection) proxies.get(name);
    }

    /**
     * Immediately unexports the remote object in the <code>exporterName</code>
     * manager, identified by the given Uuid
     */
    public synchronized void relinquish(String exporterName, Uuid identity) {
        List namedProxies = (List) proxies.get(exporterName.toLowerCase());
        ExpProxyHolder toRemove = null;
        synchronized (namedProxies) {
            for (Iterator iter = namedProxies.iterator(); iter.hasNext();) {
                ExpProxyHolder exp = (ExpProxyHolder) iter.next();
                if (exp.getID().equals(identity)) {
                    l.finest("Marked for removal..");
                    toRemove = exp;
                    break;
                }
            }

        }
        synchronized (namedProxies) {
            if (toRemove != null) {
                if (namedProxies.remove(toRemove)) {
                    holders.remove(identity);
                    l.finest("Proxy removed from List");
                } else
                    l.finest("Proxy not remove from List");
                toRemove.clear();
            }
        }
    }

    public boolean contains(Uuid identity) {
        return false;
    }

    public boolean containsIn(Uuid identity, String exporterName) {
        List namedProxies = (List) proxies.get(exporterName.toLowerCase());
        for (Iterator iter = namedProxies.iterator(); iter.hasNext();) {
            ExpProxyHolder exp = (ExpProxyHolder) iter.next();
            if (exp.getID().equals(identity))
                return true;
        }

        return false;
    }

    /**
     * Immediately unexports all remote objects in all managers
     */
    public synchronized void relinquishAll() {
        for (Iterator clearIter = proxies.entrySet().iterator(); clearIter.hasNext();) {
            java.util.Map.Entry ent = (java.util.Map.Entry) clearIter.next();
            List namedProxies = (List) ent.getValue();
            l.info("Clearing exported objects in the " + ent.getKey() + " manager");
            ExpProxyHolder exp;
            for (Iterator iter = namedProxies.iterator(); iter.hasNext(); exp.clear()) {
                exp = (ExpProxyHolder) iter.next();
                iter.remove();
                exp.clear();
            }

        }

    }

    /**
     * Immediately unexports all remote objects in the given manager
     */
    public synchronized void relinquishAll(String exporterName) {
        List namedProxies = (List) proxies.get(exporterName.toLowerCase());
        l.info("Clearing exported objects in the " + exporterName + " manager");
        ExpProxyHolder exp;
        for (Iterator iter = namedProxies.iterator(); iter.hasNext(); exp.clear()) {
            exp = (ExpProxyHolder) iter.next();
            iter.remove();
            exp.clear();
        }

    }

    /**
     * Returns the proxy (identified by the given Uuid) as returned by
     * <code>ProxyCreator#create</code>
     */
    public Object getCreatedProxy(Uuid id) {
        Object ob = holders.get(id);
        if (ob != null) {
            ExpProxyHolder holder = (ExpProxyHolder) ob;
            return holder.getWrapperedProxy();
        } else {
            return null;
        }
    }

    /**
     * Returns the exported proxy (idenitifed by the given Uuid) as returned
     * from the exporter
     */
    public Object getExportedProxy(Uuid id) {
        Object ob = holders.get(id);
        if (ob != null) {
            ExpProxyHolder holder = (ExpProxyHolder) ob;
            return holder.getExportedProxy();
        } else {
            return null;
        }
    }

    /**
     * Schedule a Relinquish Job for unexporting the given remote object after a
     * specified time in ms
     */
    public void scheduleRelinquish(String exporterName, Uuid identity, long delay) {
        synchronized (scheduleWorkList) {
            scheduleWorkList.add(new RelinquishJob(exporterName, identity, delay));
        }
    }

    public static void displayManagers(){
    		for(Map.Entry<String,ExporterManager> expMgr : exportManagers.entrySet()){
    			System.out.println("Manager: " + expMgr.getKey());
    			for (String s: expMgr.getValue().getExporterNames())
    				System.out.println("\t" + s);
    		}
    }
    
    public  String[] getExporterNames(){
    		Set s = exporters.keySet();
    		String[] retArray = new String[s.size()];
    		int i=0;
    		System.out.println(s.toString());
    		for(Iterator iter=s.iterator();iter.hasNext();){
    			String expName = (String) iter.next();
    			retArray[i++] = expName;
    		}
    		return retArray;
    }
    
    private static Map<String,ExporterManager> exportManagers = new HashMap<String,ExporterManager>();

    private ArrayList scheduleWorkList;

    private Map exporters;

    private Map proxies;

    private Map holders;

    private String name;

    private Logger l;

    private Configuration config;

    private Thread reaperThread;

}
