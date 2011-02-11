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
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.JoinManager;

/**
 *
 * @author calum
 */
public interface ServiceBuilder {
    public ServiceBuilderData initService(Remote serviceObject, Configuration config, String exporterTypeName,ActivationID id);
    public ServiceBuilderData initLocalService(Serializable serviceObject, Configuration config, String exporterTypeName,ActivationID id);
}
