/*
 * QueueListener.java
 *
 * Created on 20 September 2006, 12:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package glyph.test.queue;

import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author calum
 */
public interface QueueListener extends Remote{
    public void deliver(Serializable aPayload, MarshalledObject aHandback) throws RemoteException;
}
