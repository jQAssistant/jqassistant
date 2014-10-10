package com.buschmais.jqassistant.core.analysis.impl;

import static com.buschmais.jqassistant.core.analysis.api.rule.Constraint.DEFAULT_SEVERITY;

import com.buschmais.jqassistant.core.analysis.api.RuleSetWriter;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ConceptType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ConstraintType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.GroupType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.IncludedConstraintType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.JqassistantRules;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.ReferenceType;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.SeverityEnumType;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Implementation of a {@link RuleSetWriter}.
 */
public class RuleSetWriterImpl implements RuleSetWriter {

    private JAXBContext jaxbContext;

    public RuleSetWriterImpl() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    @Override
    public void write(RuleSet ruleSet, Writer writer) {
        SortedMap<String, Group> groups = new TreeMap<>();
        SortedMap<String, Concept> concepts = new TreeMap<>();
        SortedMap<String, Constraint> constraints = new TreeMap<>();
        for (Group group : ruleSet.getGroups().values()) {
            addGroup(group, groups, concepts, constraints);
        }
        JqassistantRules rules = new JqassistantRules();
        writeGroups(groups.values(), rules);
        writeConcepts(concepts.values(), rules);
        writeConstraints(constraints.values(), rules);

        marshal(writer, rules);
    }

    private void marshal(Writer writer, JqassistantRules rules) {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = null;
        try {
            streamWriter = xof.createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        XMLStreamWriter indentingStreamWriter = new IndentingXMLStreamWriter(new CDataXMLStreamWriter(streamWriter));
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(rules, indentingStreamWriter);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot write rules to " + writer, e);
        }
    }

    private void addGroup(Group group, Map<String, Group> groups, Map<String, Concept> concepts, Map<String, Constraint> constraints) {
        if (!groups.containsKey(group.getId())) {
            groups.put(group.getId(), group);
            for (Group includeGroup : group.getGroups()) {
                addGroup(includeGroup, groups, concepts, constraints);
            }
            for (Concept concept : group.getConcepts()) {
                addConcept(concept, concepts);
            }
            for (Constraint constraint : group.getConstraints()) {
                addConstraint(constraint, concepts, constraints);
            }
        }
    }

    private void addConcept(Concept concept, Map<String, Concept> concepts) {
        if (!concepts.containsKey(concept.getId())) {
            concepts.put(concept.getId(), concept);
            addRequiredConcepts(concept, concepts);
        }
    }

    private void addConstraint(Constraint constraint, Map<String, Concept> concepts, Map<String, Constraint> constraints) {
        if (!constraints.containsKey(constraint.getId())) {
            constraints.put(constraint.getId(), constraint);
            addRequiredConcepts(constraint, concepts);
        }
    }

    private void addRequiredConcepts(AbstractRule executable, Map<String, Concept> concepts) {
        for (Concept concept : executable.getRequiresConcepts()) {
            addConcept(concept, concepts);
        }
    }

    private void writeGroups(Collection<Group> groups, JqassistantRules rules) {
        for (Group group : groups) {
            GroupType groupType = new GroupType();
            groupType.setId(group.getId());
            for (Group includeGroup : group.getGroups()) {
                ReferenceType groupReferenceType = new ReferenceType();
                groupReferenceType.setRefId(includeGroup.getId());
                groupType.getIncludeGroup().add(groupReferenceType);
            }
            for (Concept includeConcept : group.getConcepts()) {
                ReferenceType conceptReferenceType = new ReferenceType();
                conceptReferenceType.setRefId(includeConcept.getId());
                groupType.getIncludeConcept().add(conceptReferenceType);
            }
            for (Constraint includeConstraint : group.getConstraints()) {
                IncludedConstraintType includedConstraintType = new IncludedConstraintType();
                includedConstraintType.setRefId(includeConstraint.getId());
                includedConstraintType.setSeverity(getSeverity(includeConstraint.getSeverity()));
                groupType.getIncludeConstraint().add(includedConstraintType);
            }
            rules.getQueryTemplateOrConceptOrConstraint().add(groupType);
        }
    }

    private void writeConcepts(Collection<Concept> concepts, JqassistantRules rules) {
        for (Concept concept : concepts) {
            ConceptType conceptType = new ConceptType();
            conceptType.setId(concept.getId());
            conceptType.setDescription(concept.getDescription());
            conceptType.setCypher(concept.getQuery().getCypher());
            for (Concept requiresConcept : concept.getRequiresConcepts()) {
                ReferenceType conceptReferenceType = new ReferenceType();
                conceptReferenceType.setRefId(requiresConcept.getId());
                conceptType.getRequiresConcept().add(conceptReferenceType);
            }
            rules.getQueryTemplateOrConceptOrConstraint().add(conceptType);
        }
    }

    private void writeConstraints(Collection<Constraint> constraints, JqassistantRules rules) {
        for (Constraint constraint : constraints) {
            ConstraintType constraintType = new ConstraintType();
            constraintType.setId(constraint.getId());
            constraintType.setDescription(constraint.getDescription());
            constraintType.setSeverity(getSeverity(constraint.getSeverity()));
            constraintType.setCypher(constraint.getQuery().getCypher());
            for (Concept requiresConcept : constraint.getRequiresConcepts()) {
                ReferenceType conceptReferenceType = new ReferenceType();
                conceptReferenceType.setRefId(requiresConcept.getId());
                constraintType.getRequiresConcept().add(conceptReferenceType);
            }
            rules.getQueryTemplateOrConceptOrConstraint().add(constraintType);
        }
    }

    /**
     * Converts {@link Severity} to {@link SeverityEnumType}
     * 
     * @param severity
     *            {@link Severity}
     * @return {@link SeverityEnumType}
     */
    private SeverityEnumType getSeverity(Severity severity) {
        if (severity == null) {
            severity = DEFAULT_SEVERITY;
        }
        return SeverityEnumType.fromValue(severity.getValue());
    }
}
