/**
 * 
 */
package org.jini.glyph;

class FileCreatedItem {

    private String type;

    private String name;

    private String packageName;

    private boolean includedInDL = true;
    
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public FileCreatedItem(String type, String name, String packageName, boolean includedInDL) {
        this.type = type;
        this.name = name;
        this.packageName = packageName;
        this.includedInDL = includedInDL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIncludedInDL() {
        return includedInDL;
    }

    public void setIncludedInDL(boolean includedInDL) {
        this.includedInDL = includedInDL;
    }
}