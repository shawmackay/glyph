/*
 * ExportableProcessor.java
 *
 * Created on 22 August 2006, 12:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jini.glyph;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jini.glyph.postprocessing.BasicPostProcessingItem;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
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
public class ServiceUIProcessor implements AnnotationProcessor {
    private AnnotationProcessorEnvironment environment;

    private TypeDeclaration asyncDeclaration;

    private DeclarationVisitor declarationVisitor;

    private PrintWriter intOutFile;

    private String packageName;

    private String originalPackageName;

    StringBuffer methodBuffer;

    StringBuffer methodDeclBuffer = new StringBuffer();

    private FinalRoundProcessor deferToFinalRound;

    public ServiceUIProcessor(AnnotationProcessorEnvironment env, FinalRoundProcessor deferToFinalRound) {
        this.deferToFinalRound = deferToFinalRound;
        environment = env;

        asyncDeclaration = environment.getTypeDeclaration("org.jini.glyph.ServiceUI");

        declarationVisitor = new AllDeclarationsVisitor();
    }

    public void process() {

        Collection<TypeDeclaration> declarations = environment.getTypeDeclarations();
        // Note here we use a helper method to create a declaration
        // scanner for our
        // visitor, and a no-op visitor.
        DeclarationVisitor scanner = DeclarationVisitors.getSourceOrderDeclarationScanner(declarationVisitor, DeclarationVisitors.NO_OP);
        for (TypeDeclaration declaration : declarations) {
            declaration.accept(scanner); // invokes the
            // processing on the
            // scanner.
        }
    }

    private class AllDeclarationsVisitor extends SimpleDeclarationVisitor {
        @Override
        public void visitDeclaration(Declaration arg0) {
            Collection<AnnotationMirror> annotations = arg0.getAnnotationMirrors();
            for (AnnotationMirror mirror : annotations) {
                // if the mirror in this iteration is for our
                // note declaration...
                if (mirror.getAnnotationType().getDeclaration().equals(asyncDeclaration)) {
                    // print out the goodies.
                    SourcePosition position = mirror.getPosition();
                    Map<AnnotationTypeElementDeclaration, AnnotationValue> values = mirror.getElementValues();

                    for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> value : values.entrySet()) {
                        if (value.getKey().getSimpleName().equals("packagename"))
                            packageName = (String) value.getValue().getValue();
                    }

                    viewClassDeclaration(arg0);

                }

            }
        }
    }

    public void viewClassDeclaration(Declaration decl) {
        TreeMap options = new TreeMap();

        try {

            ClassDeclaration cdecl = (ClassDeclaration) decl;

            methodBuffer = new StringBuffer();
            String template = "";
            ServiceUI uiannoation = cdecl.getAnnotation(ServiceUI.class);
            String componentType = null;
            if (uiannoation.factory().equals("default")) {
                componentType = cdecl.getSuperclass().getDeclaration().getSimpleName();
                // We need to default down to JComponentFactory if we can't find
                // an appropriate serviceUI factory
                if (!componentType.equals("Dialog"))
                    if (!componentType.equals("JDialog"))
                        if (!componentType.equals("Frame"))
                            if (!componentType.equals("JFrame"))
                                if (!componentType.equals("JWindow"))
                                    if (!componentType.equals("Window"))
                                        if (!componentType.equals("Panel"))
                                            componentType = "JComponent";
            } else
                componentType = uiannoation.factory();
      
            ContentTemplate factoryTmpl = new ContentTemplate(getClass().getResource("templates/serviceui/" + componentType + "Factory.tmpl"));

            Collection<com.sun.mirror.type.InterfaceType> parentInterfaces = cdecl.getSuperinterfaces();

            StringBuffer parentIntfBuffer = new StringBuffer();

            options.put("parentInterfaces", parentIntfBuffer.toString());
            options.put("remoteParentInterfaces", parentIntfBuffer.toString());
            String className = cdecl.getSimpleName();
            if (packageName == null) {
                packageName = cdecl.getPackage().getQualifiedName();
                originalPackageName = cdecl.getPackage().getQualifiedName();
            }
            // System.out.println("Package Name: " + packageName);
            options.put("fqName", "$[packageName].$[className]");
            options.put("className", className);
            options.put("packageName", packageName);
            options.put("originalPackageName", originalPackageName);
            options.put("proxyCreator", packageName + ".constrainable." + className + "Creator");
            StringBuffer testBuffer = new StringBuffer();

            BasicPostProcessingItem bppi = new BasicPostProcessingItem();
            bppi.setCategory("attributesEntry");
            Map ppoptions = new HashMap();

            ppoptions.put("className", className);
            ppoptions.put("fqName", "$[packageName].$[className]");
            ppoptions.put("packageName", packageName);
            ppoptions.put("factoryType", componentType);
            ServiceUI svcannotation = cdecl.getAnnotation(ServiceUI.class);
            String role = svcannotation.role().toLowerCase();
            if (role.startsWith("main"))
                ppoptions.put("role", "net.jini.lookup.ui.MainUI");
            if (role.startsWith("about"))
                ppoptions.put("role", "net.jini.lookup.ui.AboutUI");
            if (role.startsWith("admin"))
                ppoptions.put("role", "net.jini.lookup.ui.AdminUI");
            bppi.setFilterValue(svcannotation.id());
            bppi.setOptions(ppoptions);
            ContentTemplate bodytempl = new ContentTemplate(getClass().getResource("templates/serviceui/serviceuiattr.tmpl"));
            bppi.setContent(bodytempl.getRawContent());
            deferToFinalRound.addPostProcessingItem(bppi);
            /*
             * 
             * 
             * 
             * UIDescriptor consoleDesc = null; try { consoleDesc = new
             * UIDescriptor(AdminConsole.ROLE, ConsoleUIFact.TOOLKIT, null, new
             * java.rmi.MarshalledObject(new ConsoleUIFact()));
             * consoleDesc.attributes = new java.util.HashSet();
             * consoleDesc.attributes.add(new
             * net.jini.lookup.ui.attribute.UIFactoryTypes(java.util.Collections.singleton(JComponentFactory.TYPE_NAME))); }
             * catch (IOException ex) { ex.printStackTrace(); }
             */
            PrintWriter factoryOutputFile = environment.getFiler().createSourceFile(packageName + ".ui." + className + "UIFactory");
            factoryOutputFile.append(factoryTmpl.getContent(options));
            factoryOutputFile.close();
            deferToFinalRound.addFileCreated("source", packageName + ".ui." + className + "UIFactory", packageName, true);
            environment.getMessager().printNotice("[ServiceUI]" + componentType + " Factory for " + packageName + "." + className);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ContentTemplateException ex) {
            ex.printStackTrace();
        }
    }

    private void writeTextFile(TreeMap options, File svcConf, ContentTemplate theTemplate) throws IOException, ContentTemplateException {
        PrintWriter mergeFile = environment.getFiler().createTextFile(com.sun.mirror.apt.Filer.Location.CLASS_TREE, "", svcConf, null);
        mergeFile.append(theTemplate.getContent(options));
        mergeFile.close();
    }

    private void extractRemoteParentInterfaces(TreeMap options, Collection<com.sun.mirror.type.InterfaceType> parentInterfaces, StringBuffer parentIntfBuffer, boolean setMainParent) {
        for (InterfaceType intfT : parentInterfaces) {
            if (!setMainParent) {
                options.put("mainParentInterface", intfT.getDeclaration().getQualifiedName());
                setMainParent = true;
            }
            addRemoteMethods(intfT.getDeclaration());
            Collection<InterfaceType> parents = intfT.getSuperinterfaces();
            for (InterfaceType parentIntf : parents) {
                // System.out.println("Checking: " + parentIntf.toString());
                if (parentIntf.toString().equals("java.rmi.Remote")) {

                    // System.out.println("Interface: " +
                    // intfT.getDeclaration().getQualifiedName() + " extends
                    // Remote");
                    parentIntfBuffer.append(intfT.getDeclaration().getQualifiedName() + ", ");
                }
            }
            extractRemoteParentInterfaces(options, parents, parentIntfBuffer, setMainParent);
        }
    }

    private void addRemoteMethods(InterfaceDeclaration declaration) {
        // TODO Auto-generated method stub
        Collection<com.sun.mirror.type.InterfaceType> parentInterfaces = declaration.getSuperinterfaces();
        for (InterfaceType intfType : parentInterfaces) {
            addRemoteMethods(intfType.getDeclaration());
        }
        for (MethodDeclaration method : declaration.getMethods()) {
            Collection<ReferenceType> exceptions = method.getThrownTypes();
            for (ReferenceType exception : exceptions)
                if (exception.toString().equals("java.rmi.RemoteException"))
                    viewMethodDeclaration(method);
        }
    }

    public void viewMethodDeclaration(MethodDeclaration decl) {
        String methodHeader = createMethodHeader(decl);

        if (methodHeader != null) {
            if (methodBuffer.indexOf(methodHeader) == -1) {

                methodDeclBuffer.append("\t" + methodHeader + ";\n\n");

                methodBuffer.append("\t" + methodHeader + "{\n");
                methodBuffer.append("\t\t");
                if (!decl.getReturnType().toString().equals("void"))
                    methodBuffer.append("return ");
                methodBuffer.append("backend." + decl.getSimpleName() + "(");
                ParameterDeclaration[] params = (ParameterDeclaration[]) decl.getParameters().toArray(new ParameterDeclaration[] {});
                for (int i = 0; i < params.length; i++) {
                    methodBuffer.append(params[i].getSimpleName());
                    if (i < params.length - 1)
                        methodBuffer.append(", ");
                }
                methodBuffer.append(");\n\t}\n");
            }
        }
    }

    public String createMethodHeader(MethodDeclaration decl) {
        StringBuffer intfBuffer = new StringBuffer();
        Collection<Modifier> mods = decl.getModifiers();
        for (Modifier mod : mods) {
            if (mod.equals(Modifier.STATIC)) {
                // System.out.println("Static Modifier detected....skipping");
                return null;
            }

        }
        intfBuffer.append("\n/*\n * Remote Version of  " + decl.getSimpleName() + "\n");
        intfBuffer.append(" * @see " + decl.getDeclaringType().getQualifiedName() + "#" + decl.toString() + "\n");
        intfBuffer.append(" */\n");
        for (Modifier mod : mods) {
            if (!mod.toString().equals("abstract"))
                intfBuffer.append(mod + " ");
        }
        intfBuffer.append(decl.getReturnType().toString() + " " + decl.getSimpleName() + "(");

        ParameterDeclaration[] params = (ParameterDeclaration[]) decl.getParameters().toArray(new ParameterDeclaration[] {});
        for (int paramloop = 0; paramloop < params.length; paramloop++) {
            ParameterDeclaration param = params[paramloop];
            TypeMirror type = param.getType();
            int dimensions = 0;
            while (type instanceof ArrayType) {
                type = ((ArrayType) type).getComponentType();
                dimensions++;
            }
            if (type instanceof DeclaredType) {
                intfBuffer.append(((DeclaredType) type).getDeclaration().getQualifiedName());
                for (int i = 0; i < dimensions; i++) {
                    intfBuffer.append("[]");
                }
                intfBuffer.append(" ");
            }
            if (type instanceof PrimitiveType) {
                intfBuffer.append(((PrimitiveType) type).getKind().name().toLowerCase());
                for (int i = 0; i < dimensions; i++) {
                    intfBuffer.append("[]");
                }
                intfBuffer.append(" ");
            }
            if (paramloop < decl.getParameters().size() - 1)
                intfBuffer.append(param.getSimpleName() + ", ");
            else
                intfBuffer.append(param.getSimpleName());
        }
        intfBuffer.append(")");
        intfBuffer.append(" throws ");
        boolean addRemoteEx = true;
        for (ReferenceType throwable : decl.getThrownTypes()) {
            if (throwable.toString().equals("java.rmi.RemoteException"))
                addRemoteEx = false;
            intfBuffer.append(throwable.toString() + ", ");
        }
        if (addRemoteEx)
            return null;
        intfBuffer.deleteCharAt(intfBuffer.length() - 2);
        return intfBuffer.toString();
    }

}
