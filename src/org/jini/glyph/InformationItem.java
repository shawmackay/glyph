package org.jini.glyph;

public class InformationItem {
    private String ID;
    private String type;
    private String className;    
    private String packageName;
    private String derivedFromClass;
    private String derivedFromPackage;
    
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getDerivedFromClass() {
        return derivedFromClass;
    }
    public void setDerivedFromClass(String derivedFromClass) {
        this.derivedFromClass = derivedFromClass;
    }
    public String getDerivedFromPackage() {
        return derivedFromPackage;
    }
    public void setDerivedFromPackage(String derivedFromPackage) {
        this.derivedFromPackage = derivedFromPackage;
    }
    public String getID() {
        return ID;
    }
    public void setID(String id) {
        ID = id;
    }
    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
