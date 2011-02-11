package glyph.test.local;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.jini.glyph.Injection;
import org.jini.glyph.LocalService;


@LocalService( administrable=true)
@Injection
public class MyLocalImpl implements MyLocal {
    
    private String greeting = "Hello";
    
    public MyLocalImpl(String[] args){
        System.out.println("Creating Local Service");
    }
    
    public String sayHello(String name) {
        System.out.println("Executing sayHello method with parameter: " + name);
        return greeting + ", " + name + "!";
    }
    
    public void setGreeting(String name){
        System.out.println("Setting Greeting to: "+ name);
        this.greeting = name;
    }
}
