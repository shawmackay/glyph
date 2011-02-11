package org.jini.glyph.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;

/**
 * @author calum
 */
public class HyperJarTask extends Task {

    private File classesJar;

    private Vector libraries = new Vector();

    private String version="";

    private File destFile;

    private int compressionratio = 5;

    public HyperJarTask() {

    }

    public void setClassesjar(File classesJar) {
        this.classesJar = classesJar;
    }

    public void addFileSet(FileSet libraries) {
        this.libraries.add(libraries);
    }

    public void setVersion(String version) {
    }

    public void setDestFile(File destFile) {
        
        this.destFile = destFile;
    }

    public void setCompression(String compression) {
        compressionratio = Integer.parseInt(compression);
    }

    public void execute() throws BuildException {

        try {
            doBuild();
      
        } catch (Exception e) {
            // TODO Handle RuntimeException
            e.printStackTrace();
        }
    }

    private void doJar(File directory, String installerName, File outputFile) throws BuildException {
        Jar jardef = new Jar();
        jardef.setDescription("jar");
        jardef.setTaskName("hjar");
      
        jardef.setProject(getProject());
        
        File f = directory;
        if (f.exists()) {
            if (f.isDirectory()) {
                jardef.setBasedir(f);
                jardef.setDestFile(outputFile);
                jardef.setIncludes("**/*.*");
                jardef.setExcludes("tmp/**/*");
                jardef.execute();
            } else {
                throw new BuildException(installerName + " hjar could not be created - " + directory + " must be a directory");
            }
        } else
            throw new BuildException(installerName + " hjar could not be created - " + directory + " must exist");
    }

    private void doBuild() throws BuildException{
        File buildDir = createTempDirs(null);
        long start = System.currentTimeMillis();
        if (classesJar.isDirectory()) {
            throw new BuildException("Please place you classes in a jar file and re-run with the name of the jar file");
        }
        runPack(classesJar.getAbsolutePath().replace('\\', '/'), classesJar.getName() + ".pack", buildDir, compressionratio);
        new File(buildDir, "lib").mkdir();
        String libs;

        List<String> libList = getLibraryList();

        for (String lib : libList) {
 
            runPack(lib, "lib" + File.separator + new File(lib).getName() + ".pack", buildDir, compressionratio);
        }

        if (!version.equals("")) {
            File NARINF = new File(buildDir, "NAR-INF");
            NARINF.mkdir();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(NARINF, "nar.version")));
                writer.write("version: " + version);
                writer.newLine();
                writer.close();
                log("Written Version File", Project.MSG_VERBOSE);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        doJar(buildDir, classesJar.getName(), destFile);
        
        long end = System.currentTimeMillis();
        log("HyperJar File created in " + (end - start) + "ms", Project.MSG_INFO);
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
           log(jarfileName + " was packed in " + (end - start) + "ms", Project.MSG_VERBOSE);
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

    private List<String> getLibraryList() {
        List<String> includingLibraries = new ArrayList<String>();
        for (Iterator itFSets = libraries.iterator(); itFSets.hasNext();) { 
            FileSet fs = (FileSet) itFSets.next();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject()); 
            String[] includedFiles = ds.getIncludedFiles();
            String dirname = fs.getDir(getProject()).getAbsolutePath().replace('\\', '/');
            for (int i = 0; i < includedFiles.length; i++) {
                String filename = includedFiles[i].replace('\\', '/');
                includingLibraries.add( dirname +"/" + filename);
            }
        }
        return includingLibraries;
    }

}