package org.jini.glyph;

import java.rmi.RemoteException;
import java.rmi.activation.ActivationID;

import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.JoinManager;

public interface AdminInit {
    public void init(JoinManager jm, LookupDiscoveryManager ldm, ActivationID theID);
}
