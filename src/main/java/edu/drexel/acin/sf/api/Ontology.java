package edu.drexel.acin.sf.api;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/6/12
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Ontology {
    FieldDescriptor getFieldDescriptor(URI uri);

    ClassDescriptor getClassDescriptor(URI uri);

    Set<ClassDescriptor> withSubclasses(URI classUri);

    List<ClassDescriptor> getDefinedClasses();

    List<FieldDescriptor> getFields(URI classUri);
}
