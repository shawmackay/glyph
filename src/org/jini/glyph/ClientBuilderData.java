package org.jini.glyph;

import net.jini.core.entry.Entry;
import net.jini.lookup.ServiceItemFilter;

public class ClientBuilderData {
    private Entry[] lookupAttributes;
    private Class[] lookupClasses;
    private ServiceItemFilter filter;
    
    public ServiceItemFilter getFilter() {
        return filter;
    }
    public void setFilter(ServiceItemFilter filter) {
        this.filter = filter;
    }
    public Entry[] getLookupAttributes() {
        return lookupAttributes;
    }
    public void setLookupAttributes(Entry[] lookupAttributes) {
        this.lookupAttributes = lookupAttributes;
    }
    public Class[] getLookupClasses() {
        return lookupClasses;
    }
    public void setLookupClasses(Class[] lookupClasses) {
        this.lookupClasses = lookupClasses;
    }
}
