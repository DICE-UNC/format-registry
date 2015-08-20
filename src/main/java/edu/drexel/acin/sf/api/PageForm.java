package edu.drexel.acin.sf.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/22/12
 * Time: 5:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class PageForm {
    private ClassDescriptor classDescriptor;
    private URI id;
    private String name;
    private final List<DataField> fields = new ArrayList<DataField>();

    public PageForm() {
    }

    public PageForm(String name, URI id, ClassDescriptor classDescriptor, List<DataField> fields) {
        this.classDescriptor = classDescriptor;
        this.id = id;
        this.name = name;
        setFields(fields);
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    public void setClassDescriptor(ClassDescriptor classDescriptor) {
        this.classDescriptor = classDescriptor;
    }

    public List<DataField> getFields() {
        return fields;
    }

    public void setFields(List<DataField> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
    }

    public void addFields(List<DataField> fields) {
        this.fields.addAll(fields);
    }

    public void addFields(DataField... fields) {
        this.fields.addAll(Arrays.asList(fields));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageForm pageForm = (PageForm) o;

        if (classDescriptor != null ? !classDescriptor.equals(pageForm.classDescriptor) : pageForm.classDescriptor != null)
            return false;
        if (fields != null ? !fields.equals(pageForm.fields) : pageForm.fields != null) return false;
        if (id != null ? !id.equals(pageForm.id) : pageForm.id != null) return false;
        if (name != null ? !name.equals(pageForm.name) : pageForm.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = classDescriptor != null ? classDescriptor.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }
}
