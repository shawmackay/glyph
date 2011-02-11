package org.jini.glyph.filters;

import net.jini.lookup.ServiceItemFilter;

public interface CompositeFilter extends ServiceItemFilter{
    public void setLeftFilter(ServiceItemFilter leftFilter);
    public void setRightFilter(ServiceItemFilter rightFilter);
}
