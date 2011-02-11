/*
 * QueueImpl.java
 *
 * Created on 20 September 2006, 13:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package glyph.test.queue;

import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;

import org.jini.glyph.Exportable;
import org.jini.glyph.Service;
import org.jini.glyph.ServiceBuilderData;



/**
 *
 * @author calum
 */

@Exportable
@Service
public class QueueImpl implements RemoteQueue {
    
    /** Creates a new instance of QueueImpl */
    public QueueImpl(String[] args) {

    }

    public void register(QueueListener aListener, long aLeaseTime, MarshalledObject aHandback) throws RemoteException {
        System.out.println("Foo from Queue Impl#Register");
    }

    public void post(Serializable aPackage) throws RemoteException {    
    System.out.println("Bar from Queue Impl#Register");
    }
    
}
