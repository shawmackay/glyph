package glyph.test.advanced;

import java.io.Serializable;



public class AddressData implements Serializable{
    private String name;
    private String[] Address;
    private String userId;
    private String telephone;
    
    public AddressData(){
    
    }
    
    public AddressData(String name, String[] address, String userId, String telephone) {
        super();
        this.name = name;
        Address = address;
        this.userId = userId;
        this.telephone = telephone;
    }
    public String[] getAddress() {
        return Address;
    }
    public void setAddress(String[] address) {
        Address = address;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
