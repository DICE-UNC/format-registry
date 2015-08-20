package edu.drexel.acin.sf.owl;

import edu.drexel.acin.sf.api.ClassDescriptor;
import edu.drexel.acin.sf.api.FieldDescriptor;
import edu.drexel.acin.sf.api.Ontology;
import edu.drexel.acin.sf.util.Constants;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 2/12/12
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class OwlOntology implements Ontology {
    private static final Logger logger = Logger.getLogger(OwlOntology.class.getName());

    private final OWLOntology o;
    private final OWLDataFactory factory;

    public OwlOntology(InputStream in) throws OWLOntologyCreationException {
        if (in == null) {
            throw new NullPointerException();
        }
        final OWLOntologyManager om = OWLManager.createOWLOntologyManager();
        o = om.loadOntologyFromOntologyDocument(in);
        factory = om.getOWLDataFactory();
        logger.info("Loaded ontology with " + o.getAxiomCount() + " axioms");
    }

    @Override
    public List<ClassDescriptor> getDefinedClasses() {
        final List<ClassDescriptor> ret = new ArrayList<ClassDescriptor>();

        for (OWLAxiom a : o.getAxioms(AxiomType.DECLARATION)) {
            OWLDeclarationAxiom oda = (OWLDeclarationAxiom) a;
            if (oda.getEntity().getEntityType() == EntityType.CLASS) {
                ret.add(getClassDescriptor(oda.getEntity().asOWLClass().getIRI().toURI()));
            }
        }

        Collections.sort(ret, ClassDescriptor.ALPHABETIC_COMPARATOR);
        return ret;
    }

    @Override
    public ClassDescriptor getClassDescriptor(URI uri) {
        return getClassDescriptor(factory.getOWLClass(IRI.create(uri)));
    }

    private ClassDescriptor getClassDescriptor(OWLClass clazz) {
        final ClassDescriptor cd = new ClassDescriptor();

        for (OWLAnnotation ann : clazz.getAnnotations(o)) {
            if (ann.getProperty().isComment()) {
                cd.setComment(((OWLLiteral) ann.getValue()).getLiteral());
            } else if (ann.getProperty().isLabel()) {
                cd.setLabel(((OWLLiteral) ann.getValue()).getLiteral());
            }
        }
        cd.setUri(clazz.getIRI().toURI());

        return cd;
    }

    @Override
    public Set<ClassDescriptor> withSubclasses(URI uri) {
        return withSubclasses(factory.getOWLClass(IRI.create(uri)));
    }

    private Set<ClassDescriptor> withSubclasses(OWLClass clazz) {
        final Set<ClassDescriptor> ret = new HashSet<ClassDescriptor>();

        ret.add(getClassDescriptor(clazz));

        for (OWLSubClassOfAxiom a : o.getAxioms(AxiomType.SUBCLASS_OF)) {
            if (a.getSuperClass().equals(clazz)) {
                ret.addAll(withSubclasses(a.getSubClass().asOWLClass()));
            }
        }

        return ret;
    }

    @Override
    public FieldDescriptor getFieldDescriptor(URI uri) {
        final OWLDataProperty dataProp = factory.getOWLDataProperty(IRI.create(uri));
        final OWLObjectProperty objProp = factory.getOWLObjectProperty(IRI.create(uri));

        final FieldDescriptor fd = new FieldDescriptor();
        for (OWLAnnotation ann : dataProp.getAnnotations(o)) {
            if (ann.getProperty().isComment()) {
                fd.setComment(((OWLLiteral) ann.getValue()).getLiteral());
            } else if (ann.getProperty().isLabel()) {
                fd.setLabel(((OWLLiteral) ann.getValue()).getLiteral());
            } else if (Constants.SORT_GROUP.equals(ann.getProperty().getIRI().toString())) {
                fd.setSortGroup(new Double(((OWLLiteral) ann.getValue()).getLiteral()));
            }
        }
        fd.setFunctional(dataProp.isFunctional(o));
        fd.setUri(uri);

        for (Object r : dataProp.getRanges(o)) {
            if (r instanceof OWLDatatype) {
                fd.setDatatype(((OWLDatatype) r).getIRI().toString());
                fd.setType(FieldDescriptor.FieldType.DATA);
            } else {
                logger.warning("SOMETHING ELSE: " + r.getClass().getName());
            }
        }
        for (Object r : objProp.getRanges(o)) {
            if (r instanceof OWLClass) {
                fd.setDatatype(((OWLClass) r).getIRI().toString());
                fd.setType(FieldDescriptor.FieldType.OBJECT);
            } else {
                logger.warning("SOMETHING ELSE: " + r.getClass().getName());
            }
        }

        return fd.getDatatype() != null ? fd : null;
    }

    /**
     * @param clazz Base owl class
     * @return Set this class and all parents up to but not including 'Thing'
     */
    private Set<OWLClass> withSuperClasses(OWLClass clazz) {
        //TODO: collapse these two methods
        final Set<OWLClass> ret = new HashSet<OWLClass>();

        for (OWLAxiom a : o.getAxioms(clazz)) {
            if (a.getAxiomType() == AxiomType.SUBCLASS_OF) {
                ret.addAll(withSuperClasses(((OWLSubClassOfAxiom) a).getSuperClass().asOWLClass()));
            }
        }

        ret.add(clazz);
        return ret;
    }

    private Set<URI> withSuperClasses(URI uri) {
        final OWLClass clazz = factory.getOWLClass(IRI.create(uri));
        final Set<OWLClass> supers = withSuperClasses(clazz);
        final Set<URI> ret = new HashSet<URI>(supers.size());
        for (OWLClass s : supers) {
            ret.add(s.getIRI().toURI());
        }
        return ret;
    }

    @Override
    public List<FieldDescriptor> getFields(URI classUri) {
        final Set<URI> domains = withSuperClasses(classUri);
        final List<FieldDescriptor> ret = new ArrayList<FieldDescriptor>();
        for (OWLAxiom a : o.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
            final OWLDataPropertyDomainAxiom dpda = (OWLDataPropertyDomainAxiom) a;
            if (domains.contains(dpda.getDomain().asOWLClass().getIRI().toURI())) {
                final FieldDescriptor fd = getFieldDescriptor(dpda.getProperty().asOWLDataProperty().getIRI().toURI());
                if (fd != null) {
                    ret.add(fd);
                }
            }
        }
        for (OWLAxiom a : o.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
            final OWLObjectPropertyDomainAxiom opda = (OWLObjectPropertyDomainAxiom) a;
            if (domains.contains(opda.getDomain().asOWLClass().getIRI().toURI())) {
                final FieldDescriptor fd = getFieldDescriptor(opda.getProperty().asOWLObjectProperty().getIRI().toURI());
                if (fd != null) {
                    ret.add(fd);
                }
            }
        }
        Collections.sort(ret, FieldDescriptor.SORT_GROUP_COMPARATOR);
        return ret;
    }
}
