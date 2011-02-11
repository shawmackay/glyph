package org.jini.glyph;

import java.util.Map;

public interface PostProcessingItem {
    public String  getCategory();
    public String getOriginatingClassName();
    public String getOriginatingPackageName();
    public String getFileName();
    public String getContent();
    public String getFilterValue();
    public Map getOptions();
    public void setOptions(Map options);
    public void setCategory(String category);
    public void setFilterValue(String filterValue);
    public void setOriginatingClassName(String originatingClassName);
    public void setOriginatingPackageName(String originatingPackageName);
    public void setFileName(String filename);
    public void setContent(String content);
}
