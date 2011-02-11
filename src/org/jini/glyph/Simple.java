package org.jini.glyph;

import glyph.test.local.TestClient;

import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.Arrays;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;

public class Simple{

    private ClientBuilder builder;

    public Simple(Configuration config) {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new RMISecurityManager());

        builder = new ClientBuilderImpl();
        builder.initClient(config, "org.jini.glyph.Simple");
        System.out.println("Waiting:");
        try {
            Thread.sleep(5000);
        } catch (Exception ex) {

        }
        ServiceItem o = builder.getService(null);
        System.out.println(o.service.getClass().getName());
        TestClient client = new TestClient();
        
        
    }

 
    
    public static void main(String[] args){
        try {
            new Simple(ConfigurationProvider.getInstance(args));
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
