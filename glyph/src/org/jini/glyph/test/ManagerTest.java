// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ManagerTest.java

package org.jini.glyph.test;

import glyph.test.article.NormalChatListener;

import java.io.PrintStream;
import java.rmi.server.ExportException;
import java.util.Collection;

import org.jini.glyph.chalice.DefaultExporterManager;
import org.jini.glyph.chalice.ExporterManager;

import net.jini.id.UuidFactory;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.http.HttpServerEndpoint;

// Referenced classes of package utilities20.export.test:
//            SimpleCreator, MyRemoteImpl

public class ManagerTest
{

    public ManagerTest()
    {
        ExporterManager mgr = DefaultExporterManager.getManager();
        try {
            Object o = mgr.exportProxy(new NormalChatListener(null), null, null);
            System.out.println("Object class is: " + o.getClass().getName());
        } catch (ExportException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        mgr.addNamedExporter("MyExporter", HttpServerEndpoint.getInstance(0), new BasicILFactory(), new SimpleCreator());
        try
        {
            net.jini.id.Uuid myID = UuidFactory.generate();
            net.jini.id.Uuid myID2 = UuidFactory.generate();
            System.out.println("MyID: " + myID);
            System.out.println("MyID2: " + myID2);
            MyRemoteIntf impl1 = new MyRemoteImpl();
            MyRemoteIntf impl2 = new MyRemoteImpl();
            mgr.exportProxy(impl1, "MyExporter", myID);
            mgr.exportProxy(impl2, "MyExporter", myID2);
            System.out.println("Held: " + mgr.getAllExportedUnder("MyExporter").size());
            System.out.println("Exported");
            Thread.sleep(5000L);
            System.gc();
            System.out.println("GC'ed....sleeping");
            Thread.sleep(5000L);
            mgr.relinquish("MyExporter", myID);
            Thread.sleep(5000L);
            System.gc();
            Thread.sleep(5000L);
            System.out.println("Held: " + mgr.getAllExportedUnder("MyExporter").size());
            System.gc();
            Thread.sleep(5000L);
            mgr.exportProxy(impl1, "MyExporter", myID);
            System.out.println("Held: " + mgr.getAllExportedUnder("MyExporter").size());
            System.out.println("Exported");
            Thread.sleep(5000L);
            System.gc();
            System.out.println("GC'ed....sleeping");
            Thread.sleep(5000L);
            mgr.relinquish("MyExporter", myID);
            Thread.sleep(5000L);
            System.gc();
            Thread.sleep(5000L);
            System.out.println("Held: " + mgr.getAllExportedUnder("MyExporter").size());
     
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception 
    {
            new ManagerTest();
//            ExporterManager mgr = DefaultExporterManager.loadManager("default", "bin/conf/msgexportmgr.config");
//            mgr = DefaultExporterManager.loadManager("default", "bin/conf/msgexportmgr.config");
//            mgr = DefaultExporterManager.loadManager("messaging", "bin/conf/msgexportmgr.config");
//            mgr = DefaultExporterManager.loadManager("blah", "bin/conf/msgexportmgr.config");
//            MyRemoteIntf impl1 = new MyRemoteImpl();
//            MyRemoteIntf impl2 = new MyRemoteImpl();
//            for(int i=0;i<1000;i++){
//            net.jini.id.Uuid myID = UuidFactory.generate();
//            mgr.exportProxy(impl1, "Standard", myID);
//            mgr.scheduleRelinquish("Standard", myID, 5000);
//            }
//            
//            Thread.sleep(15000);
//            mgr.getCount();
//            System.exit(0);
   DefaultExporterManager.loadManagersFromDir("conf/expdir");
    }
}
