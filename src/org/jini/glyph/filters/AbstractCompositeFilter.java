package org.jini.glyph.filters;

import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.ServiceItemFilter;

public abstract class AbstractCompositeFilter implements CompositeFilter {

  protected ServiceItemFilter left;
  protected ServiceItemFilter right;
    
    public void setLeftFilter(ServiceItemFilter leftFilter) {
        this.left = left;
    }

    public void setRightFilter(ServiceItemFilter rightFilter) {
      this.right = right;
    }

  

}
