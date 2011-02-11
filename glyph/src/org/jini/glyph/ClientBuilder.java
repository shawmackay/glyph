/*
 * ServiceBuilder.java
 *
 * Created on 20 September 2006, 12:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jini.glyph;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.activation.ActivationID;

import net.jini.config.Configuration;
import net.jini.core.lookup.ServiceItem;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceItemFilter;

/**
 *
 * @author calum
 */
public interface ClientBuilder {
    public void  initClient(Configuration config,String componentName);
    public ServiceItem getService(ServiceItemFilter filter);
}
