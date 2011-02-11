package org.jini.glyph;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.DeclarationVisitors;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

/**
 * 
 * @author calum
 */
public class ClientProcessor implements AnnotationProcessor {
    private AnnotationProcessorEnvironment environment;

    private TypeDeclaration asyncDeclaration;

    private DeclarationVisitor declarationVisitor;

    private PrintWriter intOutFile;

    private String packageName;

    private String originalPackageName;

    StringBuffer methodBuffer;

    StringBuffer methodDeclBuffer;

    private FinalRoundProcessor deferToFinalRound;

    public ClientProcessor(AnnotationProcessorEnvironment env, FinalRoundProcessor deferToFinalRound) {
        this.deferToFinalRound = deferToFinalRound;
        environment = env;

        asyncDeclaration = environment.getTypeDeclaration("org.jini.glyph.Client");

        declarationVisitor = new AllDeclarationsVisitor();
        // deferToFinalRound.addPostProcessingItem(new ConfigEntry("somewhere",
        // " new Name(\"Blah\");"));
    }

    public void process() {
        methodBuffer = new StringBuffer();
        methodDeclBuffer = new StringBuffer();
        Collection<TypeDeclaration> declarations = environment.getTypeDeclarations();
        DeclarationVisitor scanner = DeclarationVisitors.getSourceOrderDeclarationScanner(declarationVisitor, DeclarationVisitors.NO_OP);
        for (TypeDeclaration declaration : declarations) {
            declaration.accept(scanner);
        }
    }

    private class AllDeclarationsVisitor extends SimpleDeclarationVisitor {
        @Override
        public void visitDeclaration(Declaration arg0) {

            Collection<AnnotationMirror> annotations = arg0.getAnnotationMirrors();
            for (AnnotationMirror mirror : annotations) {
                // if the mirror in this iteration is for our
                // note declaration...
                methodBuffer = new StringBuffer();
                if (mirror.getAnnotationType().getDeclaration().equals(asyncDeclaration)) {
                    // print out the goodies.
                    SourcePosition position = mirror.getPosition();
                    Map<AnnotationTypeElementDeclaration, AnnotationValue> values = mirror.getElementValues();

                    for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> value : values.entrySet()) {
                        if (value.getKey().getSimpleName().equals("packagename"))
                            packageName = (String) value.getValue().getValue();
                    }
                    System.out.println(arg0.getSimpleName());
                    viewClassDeclaration(arg0);
                }
            }
        }
    }

    public void viewClassDeclaration(Declaration decl) {
        TreeMap options = new TreeMap();
        methodBuffer = new StringBuffer();
        try {
            // TODO Auto-generated method stub
            // System.out.println("Dec SimpleName: " +
            // decl.getSimpleName());
            // System.out.println("Dec Type: " + decl.getClass().getName());
            ContentTemplate clientTmpl = new ContentTemplate(getClass().getResource("templates/client.tmpl"));

            ContentTemplate clientconfigTmpl = new ContentTemplate(getClass().getResource("templates/clientconfig.tmpl"));
            ContentTemplate clientstartconfigTmpl = new ContentTemplate(getClass().getResource("templates/clientstarter.tmpl"));
//           ContentTemplate startactconfigTmpl = new ContentTemplate(getClass().getResource("templates/clientstarteract.tmpl"));
//            ContentTemplate startactgroupconfigTmpl = new ContentTemplate(getClass().getResource("templates/servicestarteractgroup.tmpl"));
            ContentTemplate policyTmpl = new ContentTemplate(getClass().getResource("templates/policy.tmpl"));
            ContentTemplate shellTmpl = null;
            String extension = null;
            if (System.getProperty("os.name").startsWith("Windows")) {
                shellTmpl = new ContentTemplate(getClass().getResource("templates/bat.tmpl"));
                extension = ".bat";
            } else {
                shellTmpl = new ContentTemplate(getClass().getResource("templates/sh.tmpl"));
                extension = ".sh";
            }

            Collection<Modifier> c = decl.getModifiers();

            MethodDeclaration cdecl = (MethodDeclaration) decl;

            
            if(cdecl.getParameters().size()!=1)
                environment.getMessager().printError("Method must have only one argument");
            ParameterDeclaration paramdec =(ParameterDeclaration)( cdecl.getParameters().toArray()[0]);
            System.out.println("Param: " + paramdec.getSimpleName());
            options.put("castClassName", paramdec.getType().toString());
            options.put("method", cdecl.getSimpleName());

            String className = cdecl.getDeclaringType().getSimpleName();
            options.put("delegateClassName", className);
            if (className.endsWith("Impl"))
                className = className.substring(0, className.indexOf("Impl"));
            if (packageName == null) {
                packageName = cdecl.getDeclaringType().getPackage().getQualifiedName();
                originalPackageName = cdecl.getDeclaringType().getPackage().getQualifiedName();
            }
            // Build a new Options map from the options passed to Apt

            Map<String, String> passedParams = new TreeMap<String, String>();

            // Put in the dafault values for some options
            options.put("activationPort", "1098");
            options.put("codebase", "http://$[dl_host]:$[dl_port]$[dl_path]$[dl_file] http://$[dl_host]:$[dl_port]$[dl_path]jsk-dl.jar");

            String outputDir = environment.getOptions().get("-d").replace('\\', '/');
            options.put("currentdir", outputDir);

            for (Map.Entry<String, String> entr : environment.getOptions().entrySet()) {

                if (entr.getKey().startsWith("-A")) {
                    String splitKey = entr.getKey().substring(2);
                    String[] parts = splitKey.split("=");
                    passedParams.put(parts[0], parts[1]);
                    options.put(parts[0], parts[1]);
                }
            }

            String scpath = passedParams.get("scriptClasspath");
            if (scpath == null)
                scpath = environment.getOptions().get("-classpath");
            scpath = scpath.replace('\\', '/');

            // Sort out the codebase setup

            if (passedParams.get("dl_host") == null)
                options.put("dl_host", "localhost");
            else
                options.put("dl_host", passedParams.get("dl_host"));

            if (passedParams.get("dl_port") == null)
                options.put("dl_port", "80");
            else
                options.put("dl_port", passedParams.get("dl_port"));

            if (passedParams.get("dl_path") == null)
                options.put("dl_path", "/");
            else {
                String value = passedParams.get("dl_path");
                String prefix = "";
                String suffix = "";
                if (!value.startsWith("/"))
                    prefix = "/";
                if (!value.endsWith("/"))
                    suffix = "/";
                options.put("dl_path", prefix + value + suffix);
            }

            if (passedParams.get("dl_file") == null)
                options.put("dl_file", "${projectName}-dl.jar");
            else
                options.put("dl_file", passedParams.get("dl_file"));

            String cb = passedParams.get("codebase");
            String svcRoot = passedParams.get("svcRoot");
            String jiniRoot = passedParams.get("jiniRoot");
            String svcName = passedParams.get("svcName");
            String projectName = passedParams.get("projectName");
            String group = passedParams.get("jinigroup");

            if (svcRoot == null)
                svcRoot = outputDir;
            if (projectName == null)
                projectName = className;
            if (svcName == null)
                svcName = className;
            if (group == null)
                group = "LookupDiscovery.ALL_GROUPS";
            else
                group = " new String[]{\"" + group + "\"}";
            boolean skipSvcStartFile = false;
            if (jiniRoot == null) {
                environment.getMessager().printNotice("Jini root option not set - will skip creation of service starter file...");
                skipSvcStartFile = true;
            }

            options.put("scriptClasspath", scpath);

            // options.put("codebase", cb);
            options.put("svcRoot", svcRoot.replace('\\', '/'));
            options.put("jiniRoot", jiniRoot.replace('\\', '/'));
            options.put("svcName", svcName);
            options.put("projectName", projectName);
            options.put("group", group);
            options.put("proxyCreator", packageName + ".constrainable." + className + "Creator");

            options.put("className", className);
            options.put("packageName", packageName);
            options.put("originalPackageName", originalPackageName);

            // Build an information item for any serviceUI's etc to get class
            // information

            InformationItem infoItem = new InformationItem();
            infoItem.setID(cdecl.getAnnotation(Client.class).id());
            infoItem.setType("client");
            infoItem.setClassName(className);
            infoItem.setPackageName(packageName);
            this.deferToFinalRound.addInformationItem(infoItem);

            

            options.put("methods", methodBuffer.toString());
            options.put("methodDecls", methodDeclBuffer.toString());
            //deferToFinalRound.addFileCreated("source", packageName + "." + options.get("delegateClassName"), packageName, true);
            //deferToFinalRound.addFileCreated("source", packageName + "." + className + "Service", packageName, true);
            writeSourceFile(options, clientTmpl, null, className + "Binder");
            environment.getMessager().printNotice("[Client] Client Binder ");
            //deferToFinalRound.addFileCreated("source", packageName + "." + className + "ServiceImpl", packageName, false);
           
            String name = "conf/" + className + ".config";
            File svcConf = new File(name);
            deferToFinalRound.addFileCreated("config", name, null, false);
            writeTextFile(options, svcConf, clientconfigTmpl);
            environment.getMessager().printNotice("[Config] Client configuration");

            if (!skipSvcStartFile) {
                String ssName = "conf/start-" + svcName + ".config";
                File ssConf = new File(ssName);

                writeTextFile(options, ssConf, clientstartconfigTmpl);
                deferToFinalRound.addFileCreated("config", ssName, null, false);
                environment.getMessager().printNotice("[Service Starter] Non-Activatable configuration");

//                name = "conf/start-activatable-" + svcName + ".config";
//                svcConf = new File(name);
//                writeTextFile(options, svcConf, startactconfigTmpl);
//                deferToFinalRound.addFileCreated("config", name, null, false);
//                environment.getMessager().printNotice("[Service Starter] Activatable configuration");

//                name = "conf/activatable-"+svcName+"-group.config";
//                svcConf = new File(name);
//                writeTextFile(options, svcConf, startactgroupconfigTmpl);
//                deferToFinalRound.addFileCreated("config", name, null, false);
//                environment.getMessager().printNotice("[Service Starter] Activation group configuration");

                name = "run-" + svcName + extension;
                File shellFile = new File(name);
                options.put("configName", "conf/start-" + svcName + ".config");
                writeTextFile(options, shellFile, shellTmpl);
                deferToFinalRound.addFileCreated("config", name, null, false);
                environment.getMessager().printNotice("[Client] Non-activatable shell script");

//                name = "run-activatable-" + svcName + extension;
//                shellFile = new File(name);
//                options.put("configName", "conf/start-activatable-${className}.config");
//                writeTextFile(options, shellFile, shellTmpl);
//                deferToFinalRound.addFileCreated("config", name, null, false);
//                environment.getMessager().printNotice("[Client] Activatable shell script");
            }

            File policyFile = new File("conf/policy-"+svcName+".all");;
            writeTextFile(options, policyFile, policyTmpl);
            deferToFinalRound.addFileCreated("config", "conf/policy-"+svcName +".all", null, false);
            environment.getMessager().printNotice("[Policy] default policy file");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ContentTemplateException ex) {
            ex.printStackTrace();
        }
    }

    private void writeSourceFile(TreeMap options, ContentTemplate template, String packageName, String className) throws IOException, ContentTemplateException {

        if (packageName == null)
            packageName = this.packageName;
        PrintWriter creatorOutputFile = environment.getFiler().createSourceFile(packageName + "." + className);
        creatorOutputFile.append(template.getContent(options));
        creatorOutputFile.close();
    }

    private void writeTextFile(TreeMap options, File svcConf, ContentTemplate theTemplate) throws IOException, ContentTemplateException {
        PrintWriter mergeFile = environment.getFiler().createTextFile(com.sun.mirror.apt.Filer.Location.CLASS_TREE, "", svcConf, null);
        mergeFile.append(theTemplate.getContent(options));
        mergeFile.close();
    }

    

    
}

