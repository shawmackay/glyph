/*
 * ServiceBuilderData.java
 *
 * Created on 20 September 2006, 13:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jini.glyph;


import java.rmi.activation.ActivationID;

import net.jini.discovery.LookupDiscoveryManager;
import net.jini.id.Uuid;
import net.jini.lookup.JoinManager;

/**
 *
 * @author calum
 */
public class ServiceBuilderData {
    
    private LookupDiscoveryManager lookupDiscoveryManager;
    
    private JoinManager joinManager;
    
    private Uuid uuid;
    
    private ActivationID actID;
    
    private Object serviceProxyObject;
    
    private Object remoteProxyObject;
    
    public ServiceBuilderData(){    
    }
    
    /** Creates a new instance of ServiceBuilderData */
    public ServiceBuilderData(LookupDiscoveryManager ldm, JoinManager jm, Uuid id, ActivationID actid) {
        this.lookupDiscoveryManager = ldm;
        this.joinManager = jm;
        this.uuid = id;
        this.actID = actid;
    }

    public ActivationID getActID() {
        return actID;
    }

    public void setActID(ActivationID actID) {
        this.actID = actID;
    }

    public LookupDiscoveryManager getLookupDiscoveryManager() {
        return lookupDiscoveryManager;
    }

    public void setLookupDiscoveryManager(LookupDiscoveryManager lookupDiscoveryManager) {
        this.lookupDiscoveryManager = lookupDiscoveryManager;
    }

    public JoinManager getJoinManager() {
        return joinManager;
    }

    public void setJoinManager(JoinManager joinManager) {
        this.joinManager = joinManager;
    }

    public Uuid getUuid() {
        return uuid;
    }

    public void setUuid(Uuid uuid) {
        this.uuid = uuid;
    }

    public Object getRemoteProxyObject() {
        return remoteProxyObject;
    }

    public void setRemoteProxyObject(Object remoteProxyObject) {
        this.remoteProxyObject = remoteProxyObject;
    }

    public Object getServiceProxyObject() {
        return serviceProxyObject;
    }

    public void setServiceProxyObject(Object serviceProxyObject) {
        this.serviceProxyObject = serviceProxyObject;
    }
    
}
