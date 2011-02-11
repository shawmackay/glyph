package org.jini.glyph;

public class GlyphUtil {
    public static AdminInit findAdminObject(Object o) throws Exception{
	String cName= o.getClass().getName();
	if(cName.endsWith("Service"))
	    cName= cName.substring(0,cName.length()-7);
	Class cl = Class.forName(cName+"AdminImpl");
	Object admin = cl.newInstance();
	return  (AdminInit) admin;
    }
}
