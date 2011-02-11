package glyph.test.simple;

import java.rmi.RemoteException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.entry.Name;
import net.jini.space.JavaSpace05;
import org.jini.glyph.Client;

public class TestClient {
	@Client
	public void writeMyEntry(JavaSpace05 myspace){
		try {
			Name ne = new Name("Joe Bloggs");
			myspace.write(ne, null, Lease.FOREVER);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
