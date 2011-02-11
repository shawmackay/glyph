/*
 * neon : org.jini.projects.neon.deploy
 * 
 * 
 * HyperJarBuilder.java
 * Created on 18-Jul-2005
 * 
 * HyperJarBuilder
 *
 */

package org.jini.glyph.pack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;

import com.sun.jini.system.MultiCommandLine;
import com.sun.jini.system.CommandLine.BadInvocationException;
import com.sun.jini.system.CommandLine.HelpOnlyException;

/**
 * @author calum
 */
public class HyperJarBuilder {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Complete method stub for main
        try {
            Map<String, String> m = parseCommandLine(args);
            if (m.get("classes") == null || m.get("destfile") == null){
                showUsage();
                System.exit(0);
            }
            String classesjar = m.get("classes");
            String destpackfile = m.get("destfile");
            String libs = m.get("libs");
            int compression = Integer.parseInt(m.get("compress"));
            String version = m.get("version");
            System.out.println("Classes:" + classesjar);
            System.out.println("Destination: " + destpackfile);

            // boolean finalise = mcl.getBoolean("final");

            try {
                new HyperJarBuilder(new File(classesjar), new File(destpackfile), libs, compression, version);
            } catch (RuntimeException e) {
                // TODO Handle RuntimeException
                showUsage();
                System.exit(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void showUsage() {
        // TODO Auto-generated method stub
        System.out.println("Hyper Jar Builder:\n");
        System.out.println("[-?] [-classes str] [-destfile str] [-libs str] [-compress int] [-version str]");
        System.out.println("\nclasses\t\t: location of main jar file");
        System.out.println("destfile\t: name of output file (please append .hjar)");
        System.out.println("libs\t\t: comma separated list of third party libraries");
        System.out.println("compress\t: amount of compression applied to internal pack files");
        System.out.println("version\t\t: a version string to apply to file (affects unpacking)");
    }

    private static Map parseCommandLine(String[] args) {
        // TODO Auto-generated method stub
        ArrayList<String> arglist = new ArrayList<String>();
        for (String arg : args)
            arglist.add(arg);
        TreeMap<String, String> options = new TreeMap<String, String>();
        options.put("compress", "5");
        for (int i = 0; i < arglist.size(); i += 2) {
            if (arglist.get(i).equals("-?")) {
                showUsage();
                System.exit(0);
            }
            if (arglist.get(i).equals("-classes"))
                options.put("classes", arglist.get(i + 1));
            if (arglist.get(i).equals("-destfile"))
                options.put("destfile", arglist.get(i + 1));
            if (arglist.get(i).equals("-libs"))
                options.put("libs", arglist.get(i + 1));
            if (arglist.get(i).equals("-compress"))
                options.put("compress", arglist.get(i + 1));
            if (arglist.get(i).equals("-version"))
                options.put("version", arglist.get(i + 1));
        }
        return options;
    }

    public HyperJarBuilder(File classes, File destfile, String libs, int compressionRatio, String version) {
        File buildDir = createTempDirs(destfile);
        long start = System.currentTimeMillis();
        if (classes.isDirectory()) {
            System.out.println("Please place you classes in a jar file and re-run this utility with the name of the jar file");
            System.exit(0);
        }
        runPack(classes.getAbsolutePath(), classes.getName() + ".pack", buildDir, compressionRatio);
        new File(buildDir, "lib").mkdir();
        if (!libs.equals("")) {
            String[] libfiles = libs.split(",");

            for (String lib : libfiles) {
                runPack(lib, "lib" + File.separator + new File(lib).getName() + ".pack", buildDir, compressionRatio);
            }
        }
        if (!version.equals("")) {
            File NARINF = new File(buildDir, "NAR-INF");
            NARINF.mkdir();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(NARINF, "nar.version")));
                writer.write("version: " + version);
                writer.newLine();
                writer.close();
                System.out.println("Written version file");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String processString = "jar -cvf " + destfile.getAbsolutePath() + " " + classes.getName() + ".pack lib NAR-INF";
        System.out.println("Building hyperjar File " + destfile.getAbsolutePath());

        try {
            Process p = Runtime.getRuntime().exec(processString, null, buildDir);
            p.waitFor();
        } catch (IOException e) {
            // TODO Handle IOException
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("HyperJar File created in " + (end - start) + "ms");
        File narBase = new File(new File(System.getProperty("user.home")), ".hjarbuild");
        deleteDir(narBase);
    }

    private void deleteDir(File dir) {
        if (dir.isFile()) {
            dir.delete();
            return;
        }
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            deleteDir(new File(dir, children[i]));
        }
        dir.delete();
    }

    private boolean runPack(String jarfileName, String outputFileName, File baseOutputDir, int compression) {
        Packer packer = Pack200.newPacker();
        Map p = packer.properties();
        // take more time choosing codings for better compression
        p.put(Packer.EFFORT, String.valueOf(compression)); // default is "5"
        // use largest-possible archive segments (>10% better compression).
        p.put(Packer.SEGMENT_LIMIT, "-1");
        // reorder files for better compression.
        p.put(Packer.KEEP_FILE_ORDER, Packer.FALSE);
        // smear modification times to a single value.
        p.put(Packer.MODIFICATION_TIME, Packer.LATEST);
        // ignore all JAR deflation requests,
        // transmitting a single request to use "store" mode.
        p.put(Packer.DEFLATE_HINT, Packer.FALSE);
        // discard debug attributes
        p.put(Packer.CODE_ATTRIBUTE_PFX + "LineNumberTable", Packer.STRIP);
        // throw an error if an attribute is unrecognized
        p.put(Packer.UNKNOWN_ATTRIBUTE, Packer.ERROR);
        // pass one class file uncompressed:
        p.put(Packer.PASS_FILE_PFX + 0, "mutants/Rogue.class");
        try {
            long start = System.currentTimeMillis();
            JarFile jarFile = new JarFile(jarfileName);
            FileOutputStream fos = new FileOutputStream(new File(baseOutputDir, outputFileName));
            // Call the packer
            packer.pack(jarFile, fos);
            jarFile.close();
            fos.close();
            long end = System.currentTimeMillis();
            System.out.println(jarfileName + " was packed in " + (end - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private File createTempDirs(File destfile) {
        File narBase = new File(new File(System.getProperty("user.home")), ".hjarbuild");
        if (!narBase.exists())
            narBase.mkdir();
        return narBase;
    }

}
