package glyph.test.advanced;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AddressEnquiry extends Remote{
    public AddressData search(String value) throws RemoteException;
}
