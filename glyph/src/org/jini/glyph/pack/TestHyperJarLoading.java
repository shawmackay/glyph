/*
 * Copyright 2005 neon.jini.org project 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.jini.glyph.pack;

/*
 * TestHyperJarLoading.java
 * 
 * Created Wed Mar 16 10:48:15 GMT 2005
 */

import java.net.URL;

import org.jini.glyph.url.hjar.NarLoader;
import org.jini.glyph.url.hjar.NarLoader.URLFileLink;


/**
 * 
 * @author calum
 * 
 */

public class TestHyperJarLoading {
    public static void main(String[] args) {
        //System.setProperty("")
        try {
            if(args.length==0){
                System.out.println("Please specify a URL for HyperJar unpack test");
            }
            NarLoader loader = new NarLoader();
            URLFileLink[] expanded=loader.deployNar(new URL(args[0]));
            System.out.println("Unpacked URLs");
            for(URLFileLink u: expanded)
                System.out.println(u.getUrlPath().toExternalForm());
        } catch (Exception ex) {
            System.err.println("Caught Exception: " + ex.getClass().getName() + "; Msg: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
