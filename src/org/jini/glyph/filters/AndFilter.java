package org.jini.glyph.filters;

import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.ServiceItemFilter;

public class AndFilter extends AbstractCompositeFilter {

    public AndFilter(ServiceItemFilter left, ServiceItemFilter right){
        this.left  = left;
        this.right = right;
    }
    
    public boolean check(ServiceItem item) {
        return left.check(item) && right.check(item);
    }

}
