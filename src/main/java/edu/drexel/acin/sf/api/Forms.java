package edu.drexel.acin.sf.api;

import edu.drexel.acin.sf.db.PostgresStorage;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.JargonQueryException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/23/12
 * Time: 9:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class Forms {
    private final Ontology ontology;
    private final FileStorage storage;
    private final PostgresStorage metaStorage;

    public Forms(Ontology ontology, FileStorage storage, PostgresStorage metaStorage) {
        this.ontology = ontology;
        this.storage = storage;
        this.metaStorage = metaStorage;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public List<ClassDescriptor> getAllClasses() {
        return ontology.getDefinedClasses();
    }

    public PageForm getForm(URI entityUri) throws SQLException {
        final URI classUri = metaStorage.getEntityClass(entityUri);
        return metaStorage.getForm(entityUri, ontology.getClassDescriptor(classUri), ontology.getFields(classUri));
    }

    public Map<URI, String> getAllEntities(URI classUri) throws SQLException {
        return metaStorage.listClassEntities(classUri);
    }

    public ClassDescriptor getClassDescriptor(URI classUri) {
        return ontology.getClassDescriptor(classUri);
    }

    public void createNewEntity(URI classUri, URI entityUri, String label) throws SQLException {
        metaStorage.createNew(entityUri, classUri, label);
    }

    public void deleteEntity(URI entityUri) throws IOException, SQLException {
        storage.deleteEntity(entityUri);
        metaStorage.deleteEntity(entityUri);
    }

    public void updateEntity(FormSubmit form) throws SQLException {
        final URI entityUri = form.getEntityUri();

        final URI classUri = metaStorage.getEntityClass(entityUri);

        final ClassDescriptor classDescriptor = ontology.getClassDescriptor(classUri);
        final List<FieldDescriptor> fieldDescriptors = ontology.getFields(classUri);

        final Map<URI, DataField> fields = new HashMap<URI, DataField>(form.getFields().size());

        for (FieldDescriptor fd : fieldDescriptors) {
            fields.put(fd.getUri(), new DataField(fd));
        }

        for (Map.Entry<URI, List<String>> entry : form.getFields().entrySet()) {
            final DataField field = fields.get(entry.getKey());
            if (field != null) {
                field.getValues().addAll(entry.getValue());
            }
        }

        metaStorage.updateValues(entityUri, fields.values());
    }

    //TODO: why bother proxying the FileStorage methods in here
    public void saveSample(URI entityUri, String filename, InputStream content) throws IOException {
        storage.saveFile(entityUri, filename, content);
    }

    public void deleteSample(URI entityUri, String filename) throws IOException {
        storage.deleteFile(entityUri, filename);
    }

    public void streamSample(URI entityUri, String filename, OutputStream out) throws IOException {
        storage.getFile(entityUri, filename, out);
    }
}
