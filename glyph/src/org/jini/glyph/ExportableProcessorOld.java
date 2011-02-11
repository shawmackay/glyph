/*
 * ExportableProcessor.java
 *
 * Created on 22 August 2006, 12:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jini.glyph;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.DeclarationVisitors;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.mirror.util.SourcePosition;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author calum
 */
public class ExportableProcessorOld implements AnnotationProcessor {
    private AnnotationProcessorEnvironment environment;
    
    private TypeDeclaration asyncDeclaration;
    
    private DeclarationVisitor declarationVisitor;
    
    private PrintWriter intOutFile;
    
    private String packageName;
    private String originalPackageName;
    
    public ExportableProcessorOld(AnnotationProcessorEnvironment env) {
        environment = env;
        // get the annotation type declaration for our 'Note'
        // annotation.
        asyncDeclaration = environment.getTypeDeclaration("org.jini.projects.neon.annotations.Exportable");
        
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
    
    private class AllDeclarationsVisitor
            extends
            SimpleDeclarationVisitor {
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
                    environment.getMessager().printNotice("Exportable source being generated: " + asyncDeclaration.toString());
                    for(Map.Entry<AnnotationTypeElementDeclaration,AnnotationValue> value: values.entrySet()){
                        if(value.getKey().getSimpleName().equals("packagename"))
                            packageName = (String) value.getValue().getValue();
                    }
                    viewClassDeclaration(arg0);
                    
                    
                    
                    
                }
                
                
            }
        }
    }
    
    
    public void viewClassDeclaration(Declaration decl) {
        // TODO Auto-generated method stub
        //System.out.println("Dec SimpleName: " + decl.getSimpleName());
        //System.out.println("Dec Type: " + decl.getClass().getName());
        Collection<Modifier> c = decl.getModifiers();
//                for(Modifier m :  c){
//                        System.out.println("Modifier: " + m.name());
//                }
//
        InterfaceDeclaration cdecl = (InterfaceDeclaration) decl;
        
        String className = cdecl.getSimpleName();
        if(packageName==null){
            packageName = cdecl.getPackage().getQualifiedName() + ".remote";
            originalPackageName = cdecl.getPackage().getQualifiedName();
        }
        System.out.println("Package Name: " + packageName);
        StringBuffer remoteIntfBuffer = new StringBuffer();
        StringBuffer remoteImplBuffer = new StringBuffer();
        StringBuffer smartProxyBuffer = new StringBuffer();
        StringBuffer creatorBuffer = new StringBuffer();
        StringBuffer testBuffer = new StringBuffer();
        buildInterfaceHeader(remoteIntfBuffer, className);
        buildImplHeader(remoteImplBuffer, className);
        buildProxyHeader(smartProxyBuffer, className);
        buildCreatorHeader(creatorBuffer, className);
        buildTestFile(testBuffer, className);
        System.out.println("Current Buffer: " + remoteIntfBuffer.toString());
        for(MethodDeclaration method : cdecl.getMethods()){
            viewMethodDeclaration(method,remoteIntfBuffer,remoteImplBuffer,  smartProxyBuffer);
        }
        remoteIntfBuffer.append("}");
        remoteImplBuffer.append("}");
         smartProxyBuffer.append("}");
        
        try {
            intOutFile = environment.getFiler().createSourceFile(packageName +"." + className);
            intOutFile.append(remoteIntfBuffer);
            intOutFile.close();
            PrintWriter implOutFile = environment.getFiler().createSourceFile(packageName +"."+className + "Impl");
            implOutFile.append(remoteImplBuffer);
            implOutFile.close();
            
            PrintWriter proxyOutputFile = environment.getFiler().createSourceFile(packageName + ".constrainable." + className + "Proxy");
            proxyOutputFile.append(smartProxyBuffer);
            proxyOutputFile.close();
            PrintWriter creatorOutputFile = environment.getFiler().createSourceFile(packageName + ".constrainable." + className + "Creator");
            creatorOutputFile.append(creatorBuffer);
            creatorOutputFile.close();
            
            PrintWriter testOutputFile = environment.getFiler().createSourceFile(packageName + ".tests.Test");
            testOutputFile.append(testBuffer);
            testOutputFile.close();
                    
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void buildInterfaceHeader(final StringBuffer remoteIntfBuffer, final String className) {
        remoteIntfBuffer.append("package ");
        remoteIntfBuffer.append(packageName + ";\n");
        remoteIntfBuffer.append("/* Generated via Exportable Annotations */\n\n");
        remoteIntfBuffer.append("public interface " + className +" extends java.rmi.Remote {");
    }
    
    private void buildImplHeader(final StringBuffer buffer, final String className) {
        buffer.append("package ");
        buffer.append(packageName + ";\n");
        buffer.append("/* Generated via Exportable Annotations */\n\n");
        buffer.append("public class " + className +"Impl implements " + className + "{\n");
        buffer.append("\tprivate " +originalPackageName +"." + className  + " backend = null;\n");
        buffer.append("\tpublic " + className + "Impl(" +originalPackageName +"." +  className + " backend){\n");
        buffer.append("\t\tthis.backend = backend;\n\t}\n\n");
        
    }
    
      private void buildCreatorHeader(final StringBuffer buffer, final String className) {
        buffer.append("package ");
        buffer.append(packageName + ".constrainable;\n");
        buffer.append("/* Generated via Exportable Annotations */\n\n");
        buffer.append("public class " + className +"Creator implements org.jini.jini.exportmgr.builder.ProxyCreator{\n");
        buffer.append("\tpublic java.rmi.Remote create(java.rmi.Remote in, net.jini.id.Uuid uuid){\n");
        buffer.append("\t\tif (in instanceof " + packageName + "." + className+ ") {\n" +
                "\t\t\tif (in instanceof net.jini.core.constraint.RemoteMethodControl) {" +
                    "\t\t\t\treturn new " + packageName + ".constrainable." + className + "Proxy.ConstrainableProxy((" + packageName + "." + className + ") in, uuid, null);\n" +
                "\t\t\t} else {" +
                "\t\t\t\treturn new " + packageName + ".constrainable." + className + "Proxy((" + packageName + "." + className + ") in, uuid);\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\treturn null;\n" +
                "\t}\n" +
                "}\n");
    }
      
      private void buildTestFile(final StringBuffer buffer, final String className){
            buffer.append("package ");
        buffer.append(packageName + ".tests;\n\n");
        buffer.append("import org.jini.jini.exportmgr.*;");
        buffer.append("/* Generated via Exportable Annotations */\n\n");
        buffer.append("public class Test" + "{\n");
        buffer.append("\tpublic static void main(String[] args) throws Exception{\n");
        buffer.append("\t\tExporterManager defaultMgr;\n\t\tdefaultMgr = DefaultExporterManager.loadManager(\"default\", net.jini.config.ConfigurationProvider.getInstance(new String[]{args[2]}));\n");
        buffer.append("\t\tObject app = Class.forName(args[0]).newInstance();\n");
        buffer.append("\t\tClass exportableClass = Class.forName(args[1]);\n");
        buffer.append("\t\tjava.rmi.Remote r = defaultMgr.exportObject(app, exportableClass, net.jini.id.UuidFactory.generate());\n");
        buffer.append("\t}\n");
        buffer.append("}\n");
        
      }
    
    private void buildProxyHeader(final StringBuffer buffer, final String className) {
        buffer.append("package ");
        buffer.append(packageName + ".constrainable;\n");
        buffer.append("/* Generated via Exportable Annotations */\n\n");
        buffer.append("public class " + className +"Proxy implements " + packageName + "." + className + ", java.io.Serializable{\n");
        buffer.append("\tfinal " + packageName + "." +className  + " backend;\n");
buffer.append("\tfinal net.jini.id.Uuid id;\n");        
        buffer.append("\tpublic static " + className + "Proxy create(" + packageName + "." +className + " server, net.jini.id.Uuid proxyId){\n");
        buffer.append("\t\tif (server instanceof net.jini.core.constraint.RemoteMethodControl)\n" +
                "\t\t\treturn new " + className + "Proxy.ConstrainableProxy(server, proxyId, null);\n" +
                "\t\telse\n" +
                "\t\t\treturn new " + className + "Proxy(server, proxyId);\n" +
                "\t}");
        buffer.append("\n\n");
        buffer.append("\t" + className + "Proxy(" +packageName + "." + className + " backend, net.jini.id.Uuid id) {\n");
        buffer.append("\t\tthis.backend = backend;\n");
        buffer.append("\t\tthis.id = id;\n\t}\n\n");
        
        buffer.append("\tpublic net.jini.id.Uuid getReferentUuid(){\n");
        buffer.append("\t\treturn this.id;\n\t}\n\n");
        buffer.append("\tpublic int hashCode(){\n");
        buffer.append("\t\treturn id.hashCode();\n\t}\n\n");
        
        buffer.append("\tpublic boolean equals(Object o){\n");
        buffer.append("\t\treturn net.jini.id.ReferentUuids.compare(this, o);\n\t}\n\n");
        
        buffer.append("\tfinal static class ConstrainableProxy extends " + className + "Proxy implements net.jini.core.constraint.RemoteMethodControl{\n");
        buffer.append("\t\tprivate static final long serialVersionUID = 1L;\n");
        buffer.append("\t\tConstrainableProxy("+packageName + "." +className + " server, net.jini.id.Uuid id, net.jini.core.constraint.MethodConstraints methodConstraints) {\n" +
                "\t\t\tsuper(constrainServer(server, methodConstraints),id);\n" +
                "\t\t}\n\n");
        buffer.append("\t\tpublic net.jini.core.constraint.RemoteMethodControl setConstraints(net.jini.core.constraint.MethodConstraints methodConstraints){\n" +
                "\t\t\treturn new " + className + "Proxy.ConstrainableProxy(backend, id, methodConstraints);\n\t\t}\n\n");
        buffer.append("\t\t public net.jini.core.constraint.MethodConstraints getConstraints(){\n" +
                "\t\t\treturn ((net.jini.core.constraint.RemoteMethodControl) backend).getConstraints();\n\t\t}\n\n");
        buffer.append("private static " +packageName + "." + className + " constrainServer(" + packageName + "." +className + " server, net.jini.core.constraint.MethodConstraints methodConstraints){\n" +
                "\t\t\treturn (" + packageName + "." +className + ") ((net.jini.core.constraint.RemoteMethodControl) server).setConstraints(methodConstraints);\n\t\t}\n\t}\n\n" );
    }
    
    
    
    public void viewMethodDeclaration(MethodDeclaration decl, StringBuffer intfBuffer, StringBuffer implBuffer, StringBuffer proxyBuffer){
        String methodHeader = createMethodHeader(decl);
        if(methodHeader!=null){
            
            intfBuffer.append("\t" + methodHeader + ";\n\n");
            StringBuffer methodBuffer = new StringBuffer();
            methodBuffer.append("\t" + methodHeader + "{\n");
            methodBuffer.append("\t\t");
            if(!decl.getReturnType().toString().equals("void"))
                methodBuffer.append("return ");
            methodBuffer.append("backend."+decl.getSimpleName() + "(");
              ParameterDeclaration[] params= (ParameterDeclaration[]) decl.getParameters().toArray(new ParameterDeclaration[]{});
              for(int i=0;i<params.length;i++){
                  methodBuffer.append(params[i].getSimpleName());
                  if(i<params.length-1)
                      methodBuffer.append(", ");
              }
              methodBuffer.append(");\n\t}\n");
              implBuffer.append(methodBuffer);
              proxyBuffer.append(methodBuffer);
        }
    }
    
    
    public String createMethodHeader(MethodDeclaration decl){
        StringBuffer intfBuffer = new StringBuffer();
        Collection<Modifier> mods = decl.getModifiers();
        for(Modifier mod: mods){
            if(mod.equals(Modifier.STATIC)){
//                                System.out.println("Static Modifier detected....skipping");
                return null;
            }
            
        }
        intfBuffer.append("\n/*\n * Remote Version of  " + decl.getSimpleName() +"\n");
        intfBuffer.append( " * @see " + decl.getDeclaringType().getQualifiedName() + "#" + decl.toString() + "\n");
        intfBuffer.append(" */\n");
        for(Modifier mod: mods){
            if(!mod.toString().equals("abstract"))
            intfBuffer.append(mod + " ");
        }
        intfBuffer.append(decl.getReturnType().toString() + " " +decl.getSimpleName() + "(");
        
        ParameterDeclaration[] params= (ParameterDeclaration[]) decl.getParameters().toArray(new ParameterDeclaration[]{});
        for(int paramloop=0;paramloop<params.length;paramloop++){
            ParameterDeclaration param= params[paramloop];
            TypeMirror type = param.getType();
            int dimensions = 0;
            while (type instanceof ArrayType){
                type = ((ArrayType) type).getComponentType();
                dimensions++;
            }
            if(type instanceof DeclaredType){
                intfBuffer.append(((DeclaredType) type).getDeclaration().getQualifiedName());
                for(int i=0 ;i< dimensions;i++){
                    intfBuffer.append("[]");
                }
                intfBuffer.append(" ");
            }
            if (paramloop<decl.getParameters().size()-1)
                intfBuffer.append(param.getSimpleName() + ", ");
            else
                intfBuffer.append(param.getSimpleName());
        }
        intfBuffer.append(")");
        intfBuffer.append(" throws ");
        for(ReferenceType throwable: decl.getThrownTypes())
            intfBuffer.append(throwable.toString() + ", ");
        intfBuffer.append("java.rmi.RemoteException");
        return intfBuffer.toString();
    }
    
    
}
