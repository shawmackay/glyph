package glyph.test.local;

import java.io.Serializable;
import java.rmi.RemoteException;

public interface MyLocal extends Serializable{
    public String sayHello(String name);
}
