package glyph.test.simple;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.jini.admin.Administrable;

public interface Hello extends Remote {
    public String sayHello(String name) throws RemoteException;
}
