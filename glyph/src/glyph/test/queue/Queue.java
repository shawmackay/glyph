/*
 * Queue.java
 *
 * Created on 20 September 2006, 12:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package glyph.test.queue;

import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;

/**
 *
 * @author calum
 */
public interface Queue {
    public void register(QueueListener aListener, long aLeaseTime, MarshalledObject aHandback) throws RemoteException;
    public void post(Serializable aPackage) throws RemoteException;
}
