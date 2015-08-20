package edu.drexel.acin.sf.db;

import edu.drexel.acin.sf.api.*;
import edu.drexel.acin.sf.util.Constants;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Isaac Simmons on 9/23/2014.
 */
public class PostgresStorage {
    private static final Logger logger = Logger.getLogger(PostgresStorage.class.getName());

    final Connection conn;

    public PostgresStorage(String url, String username, String password) throws SQLException {
        this.conn = DriverManager.getConnection(url, username, password);
    }

    public static final Map<String, String> BOOLEAN_MAP;
    static {
        final Map<String, String> tmp = new HashMap<>();
        tmp.put("true", "true");
        tmp.put("false", "false");
        BOOLEAN_MAP = Collections.unmodifiableMap(tmp);
    }

    private Map<String, String> getAllowedValues(String datatype) throws SQLException {
        if (Constants.XML_BOOLEAN.equals(datatype)) {
            return BOOLEAN_MAP;
        }
        if (Constants.XML_STRING.equals(datatype)
                || Constants.XML_DATE.equals(datatype)
                || Constants.XML_URI.equals(datatype)) {
            return Collections.emptyMap();
        }

        final URI dataClassUri;
        try {
            dataClassUri = URI.create(datatype);
        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, "Invalid URI: " + datatype);
            throw ex;
        }
        final Map<URI, String> classEntities = listClassEntities(dataClassUri);

        if (classEntities == null || classEntities.isEmpty()) {
            return Collections.emptyMap();
        }

        //TODO: can I just push URI to wherever this is used? It'd save a bunch of stupid re-packaging
        final Map<String, String> allowedValues = new HashMap<>(classEntities.size());
        for(Map.Entry<URI, String> entry: classEntities.entrySet()) {
            allowedValues.put(entry.getKey().toString(), entry.getValue());
        }
        return allowedValues;
    }


    public SortedMap<URI, String> listClassEntities(URI classUri) throws SQLException{
        try (PreparedStatement stmt = conn.prepareStatement("SELECT uri, name FROM entities WHERE class = ?")) {
            stmt.setString(1, classUri.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                final SortedMap<URI, String> result = new TreeMap<>();
                while (rs.next()) {
                    try {
                        result.put(new URI(rs.getString(1)), rs.getString(2));
                    } catch (URISyntaxException ex) {
                        throw new SQLException("Malformed URI in database", ex);
                    }
              }
                return result;
            }
        }
    }


    public PageForm getForm(URI entityUri, ClassDescriptor classDescriptor, List<FieldDescriptor> fieldDescriptors) throws SQLException {
        //TODO: move Ontology into this class, then I won't have to pass those extra args in
        //TODO: but then I really ought to just use a triple store instead of postgres
        final PageForm form = new PageForm();
        form.setClassDescriptor(classDescriptor);
        form.setId(entityUri);
        final List<DataField> fields = new ArrayList<>();
        final String name;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT name FROM entities WHERE uri = ?")) {
            stmt.setString(1, entityUri.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (! rs.next()) {
                    throw new SQLException("No rows found for entity " + entityUri.toString());
                }
                name = rs.getString(1);
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement("SELECT value FROM values WHERE entity = ? and uri = ?")) {
            for (FieldDescriptor fd: fieldDescriptors) {
                final DataField field = new DataField(fd);
                //TODO: is allowed values not used??
                field.setAllowedValues(getAllowedValues(fd.getDatatype()));
                stmt.clearParameters();
                stmt.setString(1, entityUri.toString());
                stmt.setString(2, fd.getUri().toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        field.addValues(rs.getString(1));
                    }
                }
                fields.add(field);
            }
        }
        return new PageForm(name, entityUri, classDescriptor, fields);
    }

    public void updateValues(URI entityUri, Collection<DataField> fields) throws SQLException {
        //Delete existing values
        //TODO: tie these together in a transaction?
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM values WHERE entity = ?")) {
            stmt.setString(1, entityUri.toString());
            stmt.executeUpdate();
        }

        //Insert updated values
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO values (entity, uri, value) VALUES (?, ?, ?)")) {
            for(DataField field: fields) {
                for(String value: field.getValues()) {
                    //TODO: use batch updates?
                    stmt.clearParameters();
                    stmt.setString(1, entityUri.toString());
                    stmt.setString(2, field.getUri().toString());
                    stmt.setString(3, value);
                    stmt.executeUpdate();
                }
            }
        }
    }

    public URI getEntityClass(URI entityUri) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT class FROM entities WHERE uri = ?")) {
            stmt.setString(1, entityUri.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (! rs.next()) {
                    throw new SQLException("No entity found with uri " + entityUri.toString());
                }
                try {
                    return new URI(rs.getString(1));
                } catch (URISyntaxException ex) {
                    throw new SQLException("Malformed URI found in database", ex);
                }
            }
        }
    }

    public void createNew(URI entityUri, URI classUri, String label) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO entities (uri, class, name) VALUES (?, ?, ?)")) {
            stmt.setString(1, entityUri.toString());
            stmt.setString(2, classUri.toString());
            stmt.setString(3, label);
            final int numRows = stmt.executeUpdate();
            if (numRows != 1) {
                throw new SQLException("Expected one row updated when creating new entity, got " + numRows);
            }
        }
    }

    public void deleteEntity(URI entityUri) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM entities WHERE uri = ? CASCADE")) {
            stmt.setString(1, entityUri.toString());
            final int numRows = stmt.executeUpdate();
            if (numRows != 1) {
                //TODO: maybe with the cascade, this won't hold true?
                throw new SQLException("Expected one row deleted when deleting entity, got " + numRows);
            }
        }
    }
}
