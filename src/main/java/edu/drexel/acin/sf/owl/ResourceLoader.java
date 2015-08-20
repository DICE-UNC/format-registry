package edu.drexel.acin.sf.owl;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ids
 * Date: 7/24/12
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceLoader {
    private ResourceLoader() {
    }

    public static OwlOntology load(String resourceName) throws IOException, OWLOntologyCreationException {
        final InputStream in = ResourceLoader.class.getResourceAsStream(resourceName);
        if (in == null) {
            throw new IOException("Unable to locate resource: " + resourceName);
        }
        return new OwlOntology(in);
    }
}
