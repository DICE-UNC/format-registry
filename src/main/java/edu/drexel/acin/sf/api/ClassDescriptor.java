package edu.drexel.acin.sf.api;

import java.net.URI;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/6/12
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassDescriptor implements Comparable<ClassDescriptor> {
    private URI uri;
    private String label;
    private String comment;

    public ClassDescriptor() {
    }

    public ClassDescriptor(URI name, String label, String comment) {
        setUri(name);
        setLabel(label);
        setComment(comment);
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getLabel() {
        if (label != null) {
            return label;
        }
        final int poundIndex = uri.toString().indexOf('#');
        return poundIndex == -1 ? uri.toString() : uri.toString().substring(poundIndex + 1);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassDescriptor that = (ClassDescriptor) o;

        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        return result;
    }

    public static final Comparator<ClassDescriptor> ALPHABETIC_COMPARATOR = new Comparator<ClassDescriptor>() {
        @Override
        public int compare(ClassDescriptor o1, ClassDescriptor o2) {
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
        }
    };

    @Override
    public int compareTo(ClassDescriptor o) {
        return getLabel().compareTo(o.getLabel());
    }
}
