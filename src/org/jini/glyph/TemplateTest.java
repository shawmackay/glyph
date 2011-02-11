package org.jini.glyph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jini.glyph.postprocessing.BasicPostProcessingItem;


public class TemplateTest {

    public TemplateTest() throws ContentTemplateException {
        
        ContentTemplate bodytempl = new ContentTemplate(getClass().getResource("templates/test/TemplateExample.tmpl"));
        
HashMap m = new HashMap();
        m.put("name", "Calum");
       m.put("color", "Deterred");
       m.put("start", "3");
       m.put("end", "6");
       

        System.out.println(bodytempl.getContent(m));
    }

    public static void main(String[] args) throws Exception {

        new TemplateTest();
    }
}
