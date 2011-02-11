package glyph.test.simple;

import java.rmi.RemoteException;

import org.jini.glyph.Exportable;
import org.jini.glyph.Service;

@Service
@Exportable
public class HelloImpl implements Hello{

    public HelloImpl(String[] args) {
    }

    public String sayHello(String name) throws RemoteException {
        System.out.println("Executing sayHello method with parameter: " + name);
        return "Hello, " + name + "!";
    }
}
