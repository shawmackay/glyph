package org.jini.glyph;

import net.jini.core.entry.Entry;

public interface AttributeBuilder {
    public Entry[] buildAttributes(Entry[] initialAttributes);
}
