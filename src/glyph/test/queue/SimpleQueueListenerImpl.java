package glyph.test.queue;

import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;

import org.jini.glyph.Exportable;



@Exportable
public class SimpleQueueListenerImpl implements QueueListener {
public void deliver(Serializable aPayload, MarshalledObject aHandback) throws RemoteException {
    // TODO Auto-generated method stub
    System.out.println("Delivering!");
}
}
