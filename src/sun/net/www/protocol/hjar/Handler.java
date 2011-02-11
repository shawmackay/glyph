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

package sun.net.www.protocol.hjar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Unpacker;

import sun.net.www.protocol.hjar.NarLoader.URLFileLink;

/**
 * @author calum
 */
public class Handler extends URLStreamHandler {
    private Map<String, URL> cacheList = new HashMap<String, URL>();

    private boolean debug = Boolean.getBoolean("glyph.url.debug");

    private static Map<String, URLFileLink[]> downloadedCache = new TreeMap<String, URLFileLink[]>();

    public static void main(String[] args) {

    }

    private class URLVersionSet {
        URL theURL;

        String version;

        public URLVersionSet(URL theurl, String version) {
            super();
            // TODO Auto-generated constructor stub
            theURL = theurl;
            this.version = version;
        }

    }

    /**
     * @see java.net.URLStreamHandler#openConnection(URL)
     */
    protected URLConnection openConnection(URL u) throws IOException {
        // System.out.println("Getting URL: " + u.toExternalForm());
        try {
            URL otherURL = new URL(u.getFile());
            // System.out.println("otherURL: " + otherURL.toExternalForm());
            String protocol = otherURL.getProtocol();
            String host = otherURL.getHost();
            int port = otherURL.getPort();

            String file = otherURL.getFile();
            String internalFile = null;
            if (file.indexOf("!/") != -1) {
                internalFile = file.substring(file.indexOf("!/") + 2);
                file = file.substring(0, file.indexOf("!/"));
            } else {
                System.out.println(u.toExternalForm());
            }
            String query = otherURL.getQuery();
            URL subURL = new URL(protocol, host, port, file);

            File newFile;
            String version = "-";
            // System.out.println(u.toExternalForm());
            // System.out.println("file is:" + file);
            URLFileLink[] fileurls;
            if (!downloadedCache.containsKey(subURL.toExternalForm())) {
                NarLoader deployer = new NarLoader();
                fileurls = deployer.deployNar(subURL);
                downloadedCache.put(subURL.toExternalForm(), fileurls);
            } else {
                //System.out.println("Returning existing URLS");
                fileurls = downloadedCache.get(subURL.toExternalForm());
            }
            if (cacheList.containsKey(u.toExternalForm())) {

                return cacheList.get(u.toExternalForm()).openConnection();
            } else {
                InputStream validStream = null;

                for (int i = 0; i < fileurls.length; i++) {
                    // System.out.println("FILEURL:" + fileurls[i]);

                    URL checkURL = new URL(fileurls[i].getUrlPath().toExternalForm() + "!/" + internalFile);
                    // System.out.println("Checking: " +
                    // checkURL.toExternalForm());
                    try {
                        validStream = checkURL.openStream();
                        // fileurls[i]);
                        cacheList.put(u.toExternalForm(), checkURL);
                        // System.out.println("Added: " + u.toExternalForm() + "
                        // to cache");
                        // System.out.println("HJAR new version - URL");
                        i = fileurls.length;
                        // System.out.println("Returning");
                        printMessage("Returning found connection for " + internalFile);
                        return checkURL.openConnection();
                    } catch (IOException ioex) {
                        // System.out.println("IO EX!!!! " + ioex.getMessage());
                    }
                }

                // System.out.println("MainFile[0]: " + mainFiles[0];
                // URL packurl = new
                // URL("pack:"+mainFiles[0].toURL().toExternalForm() +
                // "!/"+internalFile);
                // System.out.println("PACKURL:" + packurl);
                printMessage("Looking for Internal File: [" + internalFile + "]");

                if (internalFile == null || internalFile.equals("")) {
                    // System.out.println("Returning " +
                    // subURL.toExternalForm());
                    return subURL.openConnection();
                }
                // }

                // printMessage("FileURLS length: " + fileurls.length);
                // printMessage("URL:"+fileurls[fileurls.length -
                // 1].getFilePath());
                // File f = new File(fileurls[fileurls.length -
                // 1].getFilePath());
                // printMessage("Will return connection to: " +
                // f.getAbsolutePath());
                // f.deleteOnExit();
                // //System.out.println("HJAR new version FILE");
                // cacheList.put(subURL.toExternalForm() + ":" + version,
                // f.toURL());
                // newFile = f;
                //
                // // System.out.println(newFile.getAbsolutePath());
                // // If we've specified a directory type URL, we need to find
                // the
                // // file
                // // and return the connection
                // // if (internalFile!=null)
                // if (newFile.isDirectory()) {
                // if (internalFile.trim().equals("")){
                // System.out.println("Return point A");
                //                    
                // return new URL(newFile.toURL().toExternalForm() +
                // "/").openConnection();
                // }else{
                // System.out.println("Return point B");
                //                    
                // return null;
                // } //throw new IOException("Not on my watch! " + internalFile
                // );
                // }
                // }
                // // throw new IOException("Blergh");
                // System.out.println("Return point C");
                // System.out.println("Returning NULL");
                // //return null;
                // return newFile.toURL().openConnection();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private InputStream download(URL url) {
        try {
            String host = url.getHost();
            int port = url.getPort();
            if (port == -1)
                port = url.getDefaultPort();

            Socket s = new Socket(host, port);
            // System.out.println("Getting Input stream");
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedInputStream bis = new BufferedInputStream(s.getInputStream(), 1024);
            // System.out.println("Getting Output stream");
            BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            // System.out.println("Writing Get request");
            bos.write("GET " + url.getPath() + " HTTP/1.0\n\n");

            bos.flush();

            // System.out.println("Reading response");

            String responseVal = (streamReader.readLine());
            ArrayList headers = new ArrayList();
            String line = streamReader.readLine();
            while (!line.equals("")) {
                // System.out.println("Line: " + line);
                headers.add(line);
                line = streamReader.readLine();
            }
            bis.read();
            long length = 0;
            for (int i = 0; i < headers.size(); i++) {
                String headerItem = (String) headers.get(i);
                if (headerItem.startsWith("Content-Length"))
                    length = Long.parseLong(headerItem.split(":")[1].trim());
            }
            // System.out.println("Requested content length is: " + length);
            int BUFFER_SIZE = 1024;
            long bytesremaining = length;
            byte[] buffer = new byte[(int) length];
            File tDownload = File.createTempFile("Test", "hjar");
            tDownload.deleteOnExit();
            // System.out.println(tDownload.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(tDownload);
            int read = 0;
            while (read != -1) {
                read = bis.read(buffer, 0, BUFFER_SIZE);
                if (read != -1)
                    fos.write(buffer, 0, read);
            }
            fos.close();
            bis.close();
            bos.close();
            BufferedInputStream returnBis = new BufferedInputStream(new FileInputStream(tDownload), BUFFER_SIZE);
            return returnBis;

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    private void printMessage(String message) {
        if (debug)
            System.out.println(message);
    }

}
