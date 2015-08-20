package edu.drexel.acin.sf.api;

import java.net.URI;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/28/12
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataField extends FieldDescriptor {
    private final List<String> values = new ArrayList<String>();
    private final Map<String, String> allowedValues = new HashMap<String, String>();

    public DataField() {
    }

    public DataField(FieldType type, URI uri, String label, Double sortGroup, String comment, String datatype, Boolean functional) {
        super(type, uri, label, sortGroup, comment, datatype, functional);
    }

    public DataField(FieldType type, URI uri, String label, Double sortGroup, String comment, String datatype, Boolean functional, List<String> values, Map<String, String> allowedValues) {
        super(type, uri, label, sortGroup, comment, datatype, functional);
        setValues(values);
        setAllowedValues(allowedValues);
    }

    public DataField(FieldDescriptor fd) {
        super(fd);
    }

    public DataField(FieldDescriptor fd, List<String> values, Map<String, String> allowedValues) {
        super(fd);
        setValues(values);
        setAllowedValues(allowedValues);
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values.clear();
        addValues(values);
    }

    public void addValues(List<String> values) {
        if (values != null) {
            this.values.addAll(values);
        }
    }

    public void addValues(String... values) {
        this.values.addAll(Arrays.asList(values));
    }

    public Map<String, String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(Map<String, String> allowedValues) {
        this.allowedValues.clear();
        addAllowedValues(allowedValues);
    }

    public void addAllowedValues(Map<String, String> allowedValues) {
        if (allowedValues != null) {
            this.allowedValues.putAll(allowedValues);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataField field = (DataField) o;

        if (!allowedValues.equals(field.allowedValues))
            return false;
        if (!values.equals(field.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + values.hashCode();
        result = 31 * result + allowedValues.hashCode();
        return result;
    }
}
