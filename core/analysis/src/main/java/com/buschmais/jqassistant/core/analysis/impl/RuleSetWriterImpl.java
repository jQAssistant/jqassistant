package com.buschmais.jqassistant.core.analysis.impl;

import static com.buschmais.jqassistant.core.analysis.api.rule.AbstractRule.DEFAULT_CONCEPT_SEVERITY;
import static com.buschmais.jqassistant.core.analysis.api.rule.AbstractRule.DEFAULT_CONSTRAINT_SEVERITY;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.RuleSetWriter;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.visitor.CollectRulesVisitor;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.*;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

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
    public void write(RuleSet ruleSet, Writer writer) throws AnalysisException {
        CollectRulesVisitor visitor = new CollectRulesVisitor();
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addGroupIds(ruleSet.getGroups().keySet())
                .addConstraintIds(ruleSet.getConstraints().keySet()).addConceptIds(ruleSet.getConcepts().keySet()).get();
        new RuleExecutor(visitor).execute(ruleSet, ruleSelection);
        JqassistantRules rules = new JqassistantRules();
        writeGroups(visitor.getGroups(), rules);
        writeConcepts(visitor.getConcepts().keySet(), rules);
        writeConstraints(visitor.getConstraints().keySet(), rules);

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

    private void writeGroups(Collection<Group> groups, JqassistantRules rules) {
        for (Group group : groups) {
            GroupType groupType = new GroupType();
            groupType.setId(group.getId());
            for (String includeGroupId : group.getGroups()) {
                ReferenceType groupReferenceType = new ReferenceType();
                groupReferenceType.setRefId(includeGroupId);
                groupType.getIncludeGroup().add(groupReferenceType);
            }
            for (Map.Entry<String, Severity> conceptEntry : group.getConcepts().entrySet()) {
                IncludedReferenceType conceptReferenceType = new IncludedReferenceType();
                conceptReferenceType.setRefId(conceptEntry.getKey());
                conceptReferenceType.setSeverity(getSeverity(conceptEntry.getValue(), DEFAULT_CONCEPT_SEVERITY));
                groupType.getIncludeConcept().add(conceptReferenceType);
            }
            for (Map.Entry<String, Severity> constraintEntry : group.getConstraints().entrySet()) {
                IncludedReferenceType constraintReferenceType = new IncludedReferenceType();
                constraintReferenceType.setRefId(constraintEntry.getKey());
                constraintReferenceType.setSeverity(getSeverity(constraintEntry.getValue(), DEFAULT_CONSTRAINT_SEVERITY));
                groupType.getIncludeConstraint().add(constraintReferenceType);
            }
            rules.getQueryTemplateOrConceptOrConstraint().add(groupType);
        }
    }

    private void writeConcepts(Collection<Concept> concepts, JqassistantRules rules) throws AnalysisException {
        for (Concept concept : concepts) {
            ConceptType conceptType = new ConceptType();
            conceptType.setId(concept.getId());
            conceptType.setDescription(concept.getDescription());
            conceptType.setSeverity(getSeverity(concept.getSeverity(), DEFAULT_CONCEPT_SEVERITY));
            conceptType.setCypher(concept.getCypher());
            for (String requiresConceptId : concept.getRequiresConcepts()) {
                ReferenceType conceptReferenceType = new ReferenceType();
                conceptReferenceType.setRefId(requiresConceptId);
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
            constraintType.setSeverity(getSeverity(constraint.getSeverity(), DEFAULT_CONSTRAINT_SEVERITY));
            constraintType.setCypher(constraint.getCypher());
            for (String requiresConceptId : constraint.getRequiresConcepts()) {
                ReferenceType conceptReferenceType = new ReferenceType();
                conceptReferenceType.setRefId(requiresConceptId);
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
     * @param defaultSeverity
     *            default severity level
     * @return {@link SeverityEnumType}
     */
    private SeverityEnumType getSeverity(Severity severity, Severity defaultSeverity) {
        if (severity == null) {
            severity = defaultSeverity;
        }
        return SeverityEnumType.fromValue(severity.getValue());
    }
}
