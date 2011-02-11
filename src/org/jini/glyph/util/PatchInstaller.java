package org.jini.glyph.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class PatchInstaller {
    public static void main(String[] args) throws Exception{
        
        if(args.length==0 || args[0].equals("--help") || args[0].equals("/?") || args[0].equals("-h")){
            System.out.println("HyperJAR Url Handler Patcher");
            System.out.println("----------------------------");
            System.out.println("Usage:");
            System.out.println("\torg.jini.glyph.util.PatchInstaller <jini-install-directory> <patch-file>");
            System.out.println("\twhere patch file is:");
            System.out.println("\t'sun.net.www' or 'org.jini.glyph'");
            return;
        }
       new PatchInstaller().doPatch(args);
    }

    private  void doPatch(String[] args) throws Exception {
        String jiniroot = args[0];
        String patchtype= args[1];
        String patchfile= null;
        if(patchtype.equals("sun.net.www"))
            patchfile="/sunprotocol.patch";
        else if (patchtype.equals("org.jini.glyph"))
            patchfile="/glyphprotocol.patch";
        else{
            System.err.println("Unknown patch file: " + patchtype);
            System.exit(1);
        }
       URL subURL = this.getClass().getResource(patchfile);
       
        InputStream is = (InputStream) subURL.getContent();
        String update = subURL.toExternalForm().replace(":", "_");
        update = update.replace("/", "-");
        File f = File.createTempFile("pack", update);
        f.deleteOnExit();
        FileOutputStream fostream = new FileOutputStream(f);
        copyStream(new BufferedInputStream(is), fostream);
        File tempdir = new File(new File(System.getProperty("java.io.tmpdir")),"protocolpatch");
        tempdir.mkdirs();
        String unJarPatch = "jar -xvf " + f.getAbsolutePath();
        System.out.println("Extracting patch....");
        Process p = Runtime.getRuntime().exec(unJarPatch, null, tempdir);
       Thread.sleep(5000);
       String unJarResources = "jar -xvf " + jiniroot + File.separatorChar + "lib" + File.separator + "jsk-resources.jar";
       System.out.println("Expanding jsk-resources....");
       Process p1 = Runtime.getRuntime().exec(unJarResources, null, tempdir);
       Thread.sleep(5000);
       File oldResources = new File(jiniroot + File.separatorChar + "lib" + File.separator + "jsk-resources.jar");
       oldResources.renameTo(new File(jiniroot + File.separatorChar + "lib" + File.separator + "jsk-resources.jar.back"));
       System.out.println("Rebuilding jsk-resources....");
       String jarNewResources = "jar -cvf " + jiniroot + File.separatorChar + "lib" + File.separator + "jsk-resources.jar .";
       Process p2 = Runtime.getRuntime().exec(jarNewResources, null, tempdir);
       Thread.sleep(5000);
       System.out.println("Patch applied!");
    }
    
    private void copyStream(InputStream in, OutputStream out) throws IOException {
        int numread = 0;
        byte[] buff = new byte[1024];
        while (numread != -1) {
            numread = in.read(buff, 0, 1024);
            if (numread != -1) {
                out.write(buff, 0, numread);
            }
        }
        out.flush();
        out.close();
        out.close();
    }
}
