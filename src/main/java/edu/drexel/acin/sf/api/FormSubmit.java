package edu.drexel.acin.sf.api;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ids
 * Date: 6/29/12
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class FormSubmit {
    private URI entityUri;
    private final Map<URI, List<String>> fields = new HashMap<URI, List<String>>();

    public URI getEntityUri() {
        return entityUri;
    }

    public void setEntityUri(URI entityUri) {
        this.entityUri = entityUri;
    }

    public Map<URI, List<String>> getFields() {
        return fields;
    }
}
