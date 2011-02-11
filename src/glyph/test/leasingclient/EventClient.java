package glyph.test.leasingclient;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;

import org.jini.glyph.Client;
import org.jini.glyph.chalice.DefaultExporterManager;

import glyph.test.leasing.SimpleTime;
import glyph.test.leasing.TimeListener;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.id.UuidFactory;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;

public class EventClient {

	LeaseRenewalManager lrm = new LeaseRenewalManager();

	@Client
	public void registerEvent(SimpleTime svc) {
		DefaultExporterManager.loadManagersFromDir("conf/expdir");
		if (svc == null)
			System.out.println("Service is null");
		else
			System.out.println("Service reference is good");
		ClientTimeListener myListener = new ClientTimeListener();
		try {
			TimeListener remoteObject = (TimeListener) DefaultExporterManager
					.getManager().exportProxy(myListener,
							myListener.getClass().getName(),
							UuidFactory.generate());

			EventRegistration evReg = svc.register((TimeListener) remoteObject);
			if (svc != null)
				System.out.println("Service:" + svc);
			System.out
					.println("Event Registration is null? " + (evReg == null));
			if (evReg != null)
				System.out.println("Event Lease is null? "
						+ (evReg.getLease() == null));

			if (evReg != null) {
				lrm.renewUntil(evReg.getLease(), Lease.FOREVER, null);
			} else {
				System.out.println("Event Registration is null");
			}
		} catch (ExportException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
