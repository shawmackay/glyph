package glyph.test.leasingclient;

import java.rmi.RemoteException;

import glyph.test.leasing.TimeListener;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.UnknownEventException;

import org.jini.glyph.Exportable;

@Exportable
public class ClientTimeListener implements TimeListener{
 public void notify(RemoteEvent theEvent) throws UnknownEventException,
		RemoteException {
	// TODO Auto-generated method stub
	 System.out.println("Client notified of event by Service");
}
}
