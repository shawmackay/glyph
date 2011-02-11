package org.jini.glyph.chalice;

public class ProxyPair {
    private Object smartProxy;
    private Object remoteExportedProxy;
    public ProxyPair(Object serviceRegisteredProxy, Object remoteExportedProxy) {
	super();
	this.smartProxy = serviceRegisteredProxy;
	this.remoteExportedProxy = remoteExportedProxy;
    }
    public Object getRemoteExportedProxy() {
        return remoteExportedProxy;
    }
    public void setRemoteExportedProxy(Object remoteExportedProxy) {
        this.remoteExportedProxy = remoteExportedProxy;
    }
    public Object getSmartProxy() {
        return smartProxy;
    }
    public void setSmartProxy(Object smartProxy) {
        this.smartProxy = smartProxy;
    }
}
