package edu.drexel.acin.sf.api;

import edu.drexel.acin.sf.util.JsonTransient;

import java.net.URI;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/6/12
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class FieldDescriptor {
    private FieldType type;
    private URI uri;
    private String label;
    @JsonTransient
    private Double sortGroup;
    private String comment;
    private String datatype;
    private Boolean functional;

    public FieldDescriptor() {
    }

    public FieldDescriptor(FieldType type, URI uri, String label, Double sortGroup, String comment, String datatype, Boolean functional) {
        this.type = type;
        this.uri = uri;
        this.label = label;
        this.sortGroup = sortGroup;
        this.comment = comment;
        this.datatype = datatype;
        this.functional = functional;
    }

    public FieldDescriptor(FieldDescriptor copy) {
        this(copy.type, copy.uri, copy.label, copy.sortGroup, copy.comment, copy.datatype, copy.functional);
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getSortGroup() {
        return sortGroup;
    }

    public void setSortGroup(Double sortGroup) {
        this.sortGroup = sortGroup;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public boolean isFunctional() {
        return functional == null ? false : functional;
    }

    public void setFunctional(Boolean functional) {
        this.functional = functional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldDescriptor that = (FieldDescriptor) o;

        if (functional != null ? !functional.equals(that.functional) : that.functional != null) return false;
        if (sortGroup != null ? !sortGroup.equals(that.sortGroup) : that.sortGroup != null) return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (datatype != null ? !datatype.equals(that.datatype) : that.datatype != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (type != that.type) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (sortGroup != null ? sortGroup.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (datatype != null ? datatype.hashCode() : 0);
        result = 31 * result + (functional != null ? functional.hashCode() : 0);
        return result;
    }

    public static enum FieldType {
        DATA,
        OBJECT
    }

    public static final Comparator<FieldDescriptor> SORT_GROUP_COMPARATOR = new Comparator<FieldDescriptor>() {
        @Override
        public int compare(FieldDescriptor o1, FieldDescriptor o2) {
            final double o1Sort = o1.getSortGroup() == null ? 0.0d : o1.getSortGroup();
            final double o2Sort = o2.getSortGroup() == null ? 0.0d : o2.getSortGroup();

            if (o1Sort != o2Sort) {
                if (o1Sort == 0.0d) {
                    return o2Sort > 0.0d ? 1 : -1;
                }
                if (o2Sort == 0.0d) {
                    return o1Sort < 0.0d ? 1 : -1;
                }
                if (o1Sort > 0.0d ^ o2Sort > 0.0d) {
                    return o1Sort < 0.0d ? 1 : -1;
                }
                return o1Sort > o2Sort ? 1 : -1;
            }

            if (o1.getLabel() != null && o2.getLabel() != null) {
                return o1.getLabel().compareTo(o2.getLabel());
            }

            if (o1.getLabel() == null ^ o2.getLabel() == null) {
                return o1.getLabel() == null ? -1 : 1;
            }

            return o1.getUri().compareTo(o2.getUri());
        }
    };
}
