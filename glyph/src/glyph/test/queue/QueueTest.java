package glyph.test.queue;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;

public class QueueTest implements DiscoveryListener {
	public static void main(String[] args){
		System.setSecurityManager(new RMISecurityManager());
		new QueueTest();
	}
		
	public QueueTest(){
		try{
		LookupDiscoveryManager ldm = new LookupDiscoveryManager(LookupDiscovery.ALL_GROUPS, null, this);
		
			synchronized(this){
				wait(0);
				
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
	}

	public void discarded(DiscoveryEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void discovered(DiscoveryEvent e) {
		// TODO Auto-generated method stub
		try {
			ServiceRegistrar reg =e.getRegistrars()[0];
			
			Object ob = reg.lookup(new ServiceTemplate(null, new Class[]{RemoteQueue.class},null));
			if(ob!=null){
			RemoteQueue que = (RemoteQueue) ob;
			que.post("Hello");
			System.exit(0);
			} else 
				System.out.println("RemoteQueueService could not be found");
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
