package org.jini.glyph;

import com.sun.mirror.apt.RoundCompleteListener;

public interface FinalRoundProcessor {
    public void addFileCreated(String type, String path, String packageName, boolean includeInDL);
    public void addInformationItem(InformationItem item);
    public void addPostProcessingItem(PostProcessingItem item);
}
