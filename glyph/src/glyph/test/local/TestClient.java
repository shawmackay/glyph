package glyph.test.local;

import org.jini.glyph.Client;

public class TestClient {
    public TestClient(){
        System.out.println("TestClient constructed");
    }
    
    @Client
    public void setToService(MyLocal service){
        service.sayHello("Calum");
    }
}
