package glyph.test.leasing;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.jini.core.event.EventRegistration;

public interface SimpleTime extends Remote{
	public EventRegistration register(TimeListener listener) throws RemoteException;
}
