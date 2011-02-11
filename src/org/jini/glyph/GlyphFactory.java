package org.jini.glyph;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import com.sun.mirror.util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Arrays;

import static java.util.Collections.*;
import static com.sun.mirror.util.DeclarationVisitors.*;

/*
 * This class is used to run an annotation processor that lists class
 * names.  The functionality of the processor is analogous to the
 * ListClass doclet in the Doclet Overview.
 */
public class GlyphFactory implements AnnotationProcessorFactory, RoundCompleteListener {

    // Process any set of annotations
    private static final Collection<String> supportedAnnotations = unmodifiableCollection(Arrays.asList("org.jini.glyph.*"));

    // No supported options
    private static final Collection<String> supportedOptions = emptySet();

    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }

    public void roundComplete(RoundCompleteEvent arg0) {
        // TODO Auto-generated method stub
        if (arg0.getRoundState().finalRound()) {
            System.out.println("Final Round detected...Waiting");
            try {
                Thread.sleep(10000);
            } catch (Exception ex) {
            }
        }
    }

    public Collection<String> supportedOptions() {
        return supportedOptions;
    }

    private BuildCompleteProcessor bcproc = null;

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env) {
       
        if (bcproc == null)
            bcproc = new BuildCompleteProcessor(env);

        ArrayList<AnnotationProcessor> aps = new ArrayList<AnnotationProcessor>();
        for (AnnotationTypeDeclaration atdecl : atds) {

            if (atdecl.getQualifiedName().equals(Exportable.class.getName())) {
                // System.out.println("Getting Exportable processor");
                aps.add(new ExportableProcessor(env, bcproc));
            }
            if (atdecl.getQualifiedName().equals(Service.class.getName())) {
                aps.add(new ServiceProcessor(env, bcproc));
            }
            if (atdecl.getQualifiedName().equals(LocalService.class.getName())) {
                aps.add(new LocalServiceProcessor(env, bcproc));
            }
            if (atdecl.getQualifiedName().equals(ServiceUI.class.getName())) {
                aps.add(new ServiceUIProcessor(env, bcproc));
            }
            if (atdecl.getQualifiedName().equals(LeasedResource.class.getName())) {
                aps.add(new LeasedResourceProcessor(env, bcproc));
            }
            if (atdecl.getQualifiedName().equals(Client.class.getName())) {
                aps.add(new ClientProcessor(env, bcproc));
            }
        }
        // We return a combined processor for all the annotations in the file
        if (aps.size() > 0)
            return AnnotationProcessors.getCompositeAnnotationProcessor(aps);
        else {
            env.getMessager().printNotice("Checking for any more final processing");
            if (bcproc.awaitingPostProcessing())
                return bcproc;
            else{
                env.getMessager().printNotice("Current Glyph processing complete.....");
                return AnnotationProcessors.NO_OP;
            }
        }
    }

}