package glyph.test.advanced;

import java.rmi.RemoteException;

import org.jini.glyph.Exportable;
import org.jini.glyph.Service;



@Service
@Exportable
public class AddressEnquiryImpl implements AddressEnquiry {

    public AddressEnquiryImpl(String[] args){
        
    }
    
    public AddressData search(String value) throws RemoteException {
        // TODO Auto-generated method stub
        AddressData testAddress = new AddressData("Joe Bloggs", new String[]{"1 Ths High Street","Anytown", "AB1 2CD"},"joeb","0123456789");
        return testAddress;
    }

}
