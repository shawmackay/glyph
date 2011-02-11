package org.jini.glyph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jini.glyph.postprocessing.BasicPostProcessingItem;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;

/**
 * A processor that executes when no other processor needs to execute. Similar
 * to an end of compilation cleanup or collation step.
 * 
 * @author calum
 * 
 */
public class BuildCompleteProcessor implements AnnotationProcessor, FinalRoundProcessor {

    AnnotationProcessorEnvironment env;

    // We always want this to do at least one round of post-processing to creat
    // ethe classdep file
    private boolean doPostProcessing = true;

    public BuildCompleteProcessor(AnnotationProcessorEnvironment env) {
        this.env = env;
    }

    public void addPostProcessingItem(PostProcessingItem item) {
        // TODO Auto-generated method stub
        String name = item.getCategory();

        Map<String, Collection<PostProcessingItem>> arr = processingMap.get(name);
        if (arr == null) {
            Map<String, Collection<PostProcessingItem>> al = new HashMap<String, Collection<PostProcessingItem>>();
            ArrayList<PostProcessingItem> coll = new ArrayList<PostProcessingItem>();
            al.put(item.getFilterValue(), coll);
            processingMap.put(name, al);
            arr = al;
        }
        arr.get(item.getFilterValue()).add(item);
    }

    public boolean awaitingPostProcessing() {
        if (!doPostProcessing)
            return !processingMap.isEmpty();
        else
            return true;
    }

    private static Map<String, Map<String, Collection<PostProcessingItem>>> processingMap = new HashMap<String, Map<String, Collection<PostProcessingItem>>>();

    private static Map<String, InformationItem> infoMap = new HashMap<String, InformationItem>();

    private static Collection<FileCreatedItem> filescreated = new ArrayList<FileCreatedItem>();

    private TreeMap<String, String> options;

    public void process() {
        options = new TreeMap<String, String>();
        for (Map.Entry<String, String> entr : env.getOptions().entrySet()) {

            if (entr.getKey().startsWith("-A")) {
                String splitKey = entr.getKey().substring(2);
                String[] parts = splitKey.split("=");
                options.put(parts[0], parts[1]);
            }
            String projectName = options.get("projectName");
            if (projectName == null)
                options.put("projectName", "glyphoutput");
        }

        for (Map.Entry<String, Map<String, Collection<PostProcessingItem>>> entr : processingMap.entrySet()) {
            if (options.get("glyphverbose") != null)
                System.out.println("[Wrapup] Processing:" + entr.getKey());
            doProcessing(env, entr.getKey(), entr.getValue());
        }
        if (options.get("glyphverbose") != null) {
            displayCreatedFiles();
        }

        processingMap.clear();
        doPostProcessing = false;
        // Run Class dep with the classpath computed from the existing classpath
        // and the generated one
        String classDepFileStem = options.get("classdepBuildFile");
        if (classDepFileStem != null)
            runClassDep(classDepFileStem, options.get("classdepBuildRoot"));
    }

    private void doProcessing(AnnotationProcessorEnvironment env, String category, Map<String, Collection<PostProcessingItem>> items) {
        Map m = new TreeMap();

        if (category.equals("attributesEntry")) {
            createAttributeBuilders(env, items, m);
        }

    }

    private void createAttributeBuilders(AnnotationProcessorEnvironment env, Map<String, Collection<PostProcessingItem>> items, Map m) {
        try {
            ContentTemplate serviceTmpl = new ContentTemplate(getClass().getResource("templates/postprocessing/attributebuilder.tmpl"));

            for (Map.Entry<String, Collection<PostProcessingItem>> filteredEntries : items.entrySet()) {
                ArrayList arr = new ArrayList();
                // System.out.println("Looking for information about: " +
                // filteredEntries.getKey());
                InformationItem info = infoMap.get("service:" + filteredEntries.getKey());
                m.put("packageName", info.getPackageName());
                m.put("className", info.getClassName());
                m.put("projectName", options.get("projectName"));

                for (PostProcessingItem item : filteredEntries.getValue())
                    arr.add(item);
                m.put("attributesEntry", arr);
                m.put("endloop", "endloop");
                try {

                    PrintWriter p = env.getFiler().createSourceFile(info.getPackageName() + "." + info.getClassName() + "AttributeBuilder");
                    p.write(serviceTmpl.getContent(m));
                    p.flush();
                    addFileCreated("source", info.getPackageName() + "." + info.getClassName() + "AttributeBuilder", info.getPackageName(), false);
                } catch (IOException e) {

                    e.printStackTrace();
                }

            }
        } catch (ContentTemplateException e) {

            e.printStackTrace();
        }
    }

    private void displayCreatedFiles() {
        System.out.println("Glyph created the following files: ");
        for (FileCreatedItem item : filescreated) {
            System.out.println("[" + item.getType() + "] " + item.getName());
        }
    }

    public void runClassDep(String outputFileName, String classpath) {
        Map m = new TreeMap();

        ArrayList<PostProcessingItem> classes = new ArrayList<PostProcessingItem>();
        ArrayList<PostProcessingItem> distinctPackages = new ArrayList<PostProcessingItem>();
        for (FileCreatedItem item : filescreated) {
            if (item.getType().equals("source") && item.isIncludedInDL()) {

                BasicPostProcessingItem bppi = new BasicPostProcessingItem();
                bppi.setCategory("classdepclasses");
                bppi.setContent(item.getName());
                addPostProcessingItem(bppi);
                boolean hasAddedPackageAlready = false;
                for (PostProcessingItem ppi : distinctPackages) {

                    if (ppi.getContent().equals(item.getPackageName()))
                        hasAddedPackageAlready = true;
                }
                if (!hasAddedPackageAlready) {
                    BasicPostProcessingItem pkg_bppi = new BasicPostProcessingItem();
                    pkg_bppi.setCategory("classdeppkgs");
                    pkg_bppi.setContent(item.getPackageName());
                    distinctPackages.add(pkg_bppi);
                }
            }
        }
        
        
        Map<String, Collection<PostProcessingItem>> classesitems = processingMap.get("classdepclasses");
        Map<String, Collection<PostProcessingItem>> pkgsitems = processingMap.get("classdeppkgs");
        try {

            ContentTemplate serviceTmpl = null;
            if (options.get("hjarlibs") != null){
                String hjarlibs = options.get("hjarlibs");
                String[] hjarrefs = hjarlibs.split(",");
                ArrayList<PostProcessingItem> hjarItems = new ArrayList<PostProcessingItem>();
                for(String ref : hjarrefs){
                    BasicPostProcessingItem bppi = new BasicPostProcessingItem();
                    bppi.setCategory("classdepclasses");
                    bppi.setContent(ref);
                    hjarItems.add(bppi);
                    
                }
                m.put("hjarlibrefs", hjarItems);
                serviceTmpl = new ContentTemplate(getClass().getResource("templates/postprocessing/classdepxmlwithhjar.tmpl"));
            }else
                serviceTmpl = new ContentTemplate(getClass().getResource("templates/postprocessing/classdepxml.tmpl"));
            if(classesitems!=null)
            for (Map.Entry<String, Collection<PostProcessingItem>> filteredEntries : classesitems.entrySet())
                for (PostProcessingItem item : filteredEntries.getValue()) {

                    classes.add(item);
                }

            m.put("packages", distinctPackages);
            m.put("classes", classes);
            m.put("classdepBuildRoot", classpath);
            m.put("projectName", options.get("projectName"));
            m.put("endloop", "endloop");

            File packagesFile = new File(outputFileName);
            try {
                PrintWriter pkgswriter = new PrintWriter(new FileWriter(packagesFile));

                pkgswriter.write(serviceTmpl.getContent(m));
                pkgswriter.flush();

                pkgswriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (ContentTemplateException e) {

            e.printStackTrace();
        }
    }

    public void addInformationItem(InformationItem item) {
        infoMap.put(item.getType() + ":" + item.getID(), item);
        if (options != null && options.get("glyphverbose") != null)
            System.out.println("Added: " + item.getType() + ":" + item.getID());

    }

    public void addFileCreated(String type, String path, String packageName, boolean includeInDL) {
        filescreated.add(new FileCreatedItem(type, path, packageName, includeInDL));

    }
}
