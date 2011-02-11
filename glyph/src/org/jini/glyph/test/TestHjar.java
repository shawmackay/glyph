package org.jini.glyph.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class TestHjar {
    public static void main(String[] args){
        try { 
            URL u=new URL("hjar:http://localhost:8085/glyphaddress-dl.hjar!/");
            System.out.println("\tCREATED hjar URL");
            URLClassLoader urlcl = new URLClassLoader(new URL[]{u}, Thread.currentThread().getContextClassLoader());
            System.out.println("\tCREATED ClassLoader");
            Class cl = urlcl.loadClass("glyph.test.advanced.AddressEnquiryAdmin");
            cl = urlcl.loadClass("glyph.test.advanced.constrainable.AddressEnquiryAdminProxy");
            cl = urlcl.loadClass(" org.jdesktop.layout.GroupLayout");
            //Class cl2 = urlcl.loadClass("org.jini.projects.neon.examples.simple.TestAgent");
            System.out.println("\tTRIED TO LOAD CLASS");
            System.out.println("Class name: " + cl.getName());
           
            try{
                Thread.sleep(10000);
            } catch (Exception ex){
                
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
        public static void download(URL url){
        try {
            String host = url.getHost();
            int port = url.getPort();
            if(port==-1)
                port = url.getDefaultPort();
            
            Socket s = new Socket(host,port);
            System.out.println("Getting Input stream");
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedInputStream bis = new BufferedInputStream(s.getInputStream(),1024);
            System.out.println("Getting Output stream");
            BufferedWriter  bos = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            System.out.println("Writing Get request");
            bos.write("GET " +url.getPath()+ " HTTP/1.0\n\n");
            
            bos.flush();
            
            System.out.println("Reading response");
            
            String responseVal =(streamReader.readLine());
            ArrayList headers = new ArrayList();
            String line = streamReader.readLine();
            while(!line.equals("")){
                System.out.println("Line: " + line);
                headers.add(line);
                line = streamReader.readLine();
            }
            bis.read();
            long length = 0;
            for(int i=0;i<headers.size();i++){
                String headerItem = (String) headers.get(i);
                if(headerItem.startsWith("Content-Length"))
                    length = Long.parseLong(headerItem.split(":")[1].trim());
            }
           System.out.println("Requested content length is: " + length);
           int BUFFER_SIZE=1024;
           long bytesremaining = length;
           byte[] buffer = new byte[(int)length];
           File tDownload = File.createTempFile("Test","hjar");
           System.out.println(tDownload.getAbsolutePath());
           FileOutputStream fos = new FileOutputStream(tDownload);
           int read =0;
           while(read!=-1){
               read = bis.read(buffer,0,BUFFER_SIZE);
               if(read!=-1)
               fos.write(buffer,0,read);               
           }
           fos.close();
            bis.close();
            bos.close();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
