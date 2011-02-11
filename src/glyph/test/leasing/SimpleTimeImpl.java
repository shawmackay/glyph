package glyph.test.leasing;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;

import org.jini.glyph.AccessibleLeasedResource;
import org.jini.glyph.Exportable;
import org.jini.glyph.LandlordHelper;
import org.jini.glyph.LeasingManager;
import org.jini.glyph.Service;

import com.sun.jini.landlord.LeasedResource;

@Service
@Exportable
public class SimpleTimeImpl implements SimpleTime, LandlordHelper{
	
	private Map<ListenerResource,TimeListener> listeners = new HashMap<ListenerResource,TimeListener>();
	private Timer timer;
	private TimerTask tt;
	
	private Configuration config;
	
	public SimpleTimeImpl(String[] args){
		
		try {
			config = ConfigurationProvider.getInstance(args);
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		timer = new Timer();
		tt = new TimerTask(){
			
			private int count=0;
			private int eventNum=1001;
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.print("Notifiying Listeners");
				for(TimeListener listener: listeners.values()){
					try {
						System.out.print(".");
						listener.notify(new RemoteEvent(new java.util.Date(),eventNum,count,null));
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownEventException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				count++;
				System.out.println();
			}
		};
		timer.scheduleAtFixedRate(tt, new java.util.Date(),60*1000L);
	
	}
	
	public EventRegistration register(TimeListener listener)
			throws RemoteException {
		// TODO Auto-generated method stub
		LeasingManager leasingmgr = new LeasingManager();
		
		try { 
			ListenerResource res = new ListenerResource(listener);
			
			Lease l = leasingmgr.addLeasedResource(res, 20000L,this);
			EventRegistration registration = new EventRegistration(1000,null,l,0);
			
			listeners.put(res,listener);
			return registration;
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LeaseDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean deallocateLease(LeasedResource resource) {
		// TODO Auto-generated method stub
		System.out.println("Removing Listener from managed list");
		Object resourceObject = ((AccessibleLeasedResource)resource).getResourceObject();
		boolean contains = listeners.containsKey(resourceObject);
		System.out.println("Contains this listener? " + contains);
		if(contains)
			return (listeners.remove(resourceObject)!=null);
		else {
			System.out.println("Doesn't contain this resource");
			return false;
		}
	}
}
