package org.jini.glyph.postprocessing;

import java.util.Map;

import org.jini.glyph.PostProcessingItem;


public class BasicPostProcessingItem implements PostProcessingItem {

    protected Map options;
    
    protected String filterValue;
    
    protected String category;
    
    protected String content;

    protected String filename;

    protected String originatingClassName;

    protected String originalPackageName;

    public void setContent(String content) {
        this.content = content;
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public void setOriginatingPackageName(String originalPackageName) {
        this.originalPackageName = originalPackageName;
    }

    public void setOriginatingClassName(String originatingClassName) {
        this.originatingClassName = originatingClassName;
    }

    public String getOriginatingPackageName() {
        return originalPackageName;
    }

    public String getOriginatingClassName() {
        return originatingClassName;
    }

    public String getContent() {
        // TODO Auto-generated method stub
        return content;
    }

    public String getFileName() {
        // TODO Auto-generated method stub
        return filename;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public Map getOptions() {
        return options;
    }

    public void setOptions(Map options) {
        this.options = options;
    }

}
