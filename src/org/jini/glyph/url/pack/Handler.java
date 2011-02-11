/*
 * thor.jini.org : org.jini.projects.thor.url.thor
 * 
 * 
 * Handler.java
 * Created on 14-Apr-2004
 * 
 * Handler
 *
 */

package org.jini.glyph.url.pack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Unpacker;

/**
 * @author calum
 */
public class Handler extends URLStreamHandler {
    private Map<String, File> cacheList = new HashMap<String, File>();
    
    private boolean debug = Boolean.getBoolean("glyph.url.debug");
    
    public static void main(String[] args) {
    }

    /**
     * @see java.net.URLStreamHandler#openConnection(URL)
     */
    protected URLConnection openConnection(URL u) throws IOException {

        try {

            URL otherURL = new URL(u.getFile());
            String host = otherURL.getHost();
            String protocol = otherURL.getProtocol();
            int port = otherURL.getPort();
            String file = otherURL.getFile();
            String query = otherURL.getQuery();
           // System.out.println("Trying to load: " + u.toExternalForm());
            String internalFile = null;
            if (file.indexOf("!/") != -1) {
                internalFile = file.substring(file.indexOf("!/") + 2);
                file = file.substring(0, file.indexOf("!/"));
            }
            URL subURL = new URL(protocol, host, port, file);
            File newFile;
            if (cacheList.containsKey(subURL.toExternalForm())) {
                newFile = cacheList.get(subURL.toExternalForm());
            } else {
                InputStream is = (InputStream) subURL.getContent();
                String update = subURL.toExternalForm().replace(":", "_");
                update = update.replace("/", "-");
                File f = File.createTempFile("pack", update);
                f.deleteOnExit();
                FileOutputStream fostream = new FileOutputStream(f);
                copyStream(new BufferedInputStream(is), fostream);

                // Unpack
                newFile = File.createTempFile("unpack", update);
                newFile.deleteOnExit();
                fostream = new FileOutputStream(newFile);
                BufferedOutputStream bos = new BufferedOutputStream(fostream, 50 * 1024);
                JarOutputStream jostream = new JarOutputStream(bos);
                Unpacker unpacker = Pack200.newUnpacker();
                long start = System.currentTimeMillis();

                unpacker.unpack(f, jostream);
                printMessage("Unpack of " + update + " took " +
                 (System.currentTimeMillis() - start) + "ms");
                jostream.close();
                fostream.close();
                cacheList.put(subURL.toExternalForm(), newFile);
            }
            if (internalFile != null) {

                URL directoryURL = new URL("jar:" + newFile.toURL().toExternalForm() + "!/" + internalFile);
                printMessage("Using DirectoryURL:" + directoryURL);
                return directoryURL.openConnection();
            } else
                return newFile.toURL().openConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        printMessage("Returning null for: " +u.toExternalForm());
        return null;
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

    private void printMessage(String message){
        if(debug)
            System.out.println(message);
    }
    
}
