package edu.drexel.acin.sf;

import edu.drexel.acin.sf.api.ClassDescriptor;
import edu.drexel.acin.sf.owl.OwlOntology;
import edu.drexel.acin.sf.owl.ResourceLoader;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ids
 * Date: 9/27/12
 * Time: 10:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class SchemaLoaderTest {
    @Test
    public void testLoadFromResource() throws IOException, OWLOntologyCreationException {
        OwlOntology o = ResourceLoader.load("/formatregistry.owl");
        for(ClassDescriptor cd: o.getDefinedClasses()) {
            o.getFields(cd.getUri());
        }
    }
}
