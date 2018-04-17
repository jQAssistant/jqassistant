package com.buschmais.jqassistant.core.rule.impl.writer;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.rule.api.executor.CollectRulesVisitor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutorConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.writer.RuleSetWriter;
import com.buschmais.jqassistant.core.rule.impl.reader.CDataXMLStreamWriter;
import com.buschmais.jqassistant.core.rule.schema.v1.*;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

/**
 * Implementation of a {@link RuleSetWriter}.
 */
public class XmlRuleSetWriter implements RuleSetWriter {

    private RuleConfiguration ruleConfiguration;

    private JAXBContext jaxbContext;

    private RuleSetExecutorConfiguration configuration = new RuleSetExecutorConfiguration();

    public XmlRuleSetWriter(RuleConfiguration ruleConfiguration) {
        this.ruleConfiguration = ruleConfiguration;
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    @Override
    public void write(RuleSet ruleSet, Writer writer) throws com.buschmais.jqassistant.core.analysis.api.rule.RuleException {
        CollectRulesVisitor visitor = new CollectRulesVisitor();
        RuleSelection ruleSelection = RuleSelection.builder().addGroupIds(ruleSet.getGroupsBucket().getIds())
                .addConstraintIds(ruleSet.getConstraintBucket().getIds()).addConceptIds(ruleSet.getConceptBucket().getIds()).build();
        new RuleSetExecutor(visitor, configuration).execute(ruleSet, ruleSelection);
        JqassistantRules rules = new JqassistantRules();
        writeGroups(visitor.getGroups(), rules);
        writeConcepts(visitor.getConcepts().keySet(), rules);
        writeConstraints(visitor.getConstraints().keySet(), rules);
        marshal(writer, rules);
    }

    private void marshal(Writer writer, JqassistantRules rules) throws RuleException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter;
        try {
            streamWriter = xof.createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw new RuleException("Cannot create stream writer.", e);
        }
        XMLStreamWriter indentingStreamWriter = new IndentingXMLStreamWriter(new CDataXMLStreamWriter(streamWriter));
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(rules, indentingStreamWriter);
        } catch (JAXBException e) {
            throw new RuleException("Cannot write rules to " + writer, e);
        }
    }

    private void writeGroups(Collection<Group> groups, JqassistantRules rules) {
        for (Group group : groups) {
            GroupType groupType = new GroupType();
            groupType.setId(group.getId());
            for (Map.Entry<String, Severity> groupEntry : group.getGroups().entrySet()) {
                IncludedReferenceType groupReferenceType = new IncludedReferenceType();
                groupReferenceType.setRefId(groupEntry.getKey());
                groupType.setSeverity(getSeverity(groupEntry.getValue(), ruleConfiguration.getDefaultGroupSeverity()));
                groupType.getIncludeGroup().add(groupReferenceType);
            }
            for (Map.Entry<String, Severity> conceptEntry : group.getConcepts().entrySet()) {
                IncludedReferenceType conceptReferenceType = new IncludedReferenceType();
                conceptReferenceType.setRefId(conceptEntry.getKey());
                conceptReferenceType.setSeverity(getSeverity(conceptEntry.getValue(), ruleConfiguration.getDefaultConceptSeverity()));
                groupType.getIncludeConcept().add(conceptReferenceType);
            }
            for (Map.Entry<String, Severity> constraintEntry : group.getConstraints().entrySet()) {
                IncludedReferenceType constraintReferenceType = new IncludedReferenceType();
                constraintReferenceType.setRefId(constraintEntry.getKey());
                constraintReferenceType.setSeverity(getSeverity(constraintEntry.getValue(), ruleConfiguration.getDefaultConstraintSeverity()));
                groupType.getIncludeConstraint().add(constraintReferenceType);
            }
            rules.getConceptOrConstraintOrGroup().add(groupType);
        }
    }

    private void writeConcepts(Collection<Concept> concepts, JqassistantRules rules) {
        for (Concept concept : concepts) {
            ConceptType conceptType = new ConceptType();
            conceptType.setId(concept.getId());
            conceptType.setDescription(concept.getDescription());
            conceptType.setSeverity(getSeverity(concept.getSeverity(), ruleConfiguration.getDefaultConceptSeverity()));
            writeExecutable(conceptType, concept);
            writeRequiredConcepts(concept, conceptType);
            rules.getConceptOrConstraintOrGroup().add(conceptType);
        }
    }

    private void writeConstraints(Collection<Constraint> constraints, JqassistantRules rules) {
        for (Constraint constraint : constraints) {
            ConstraintType constraintType = new ConstraintType();
            constraintType.setId(constraint.getId());
            constraintType.setDescription(constraint.getDescription());
            constraintType.setSeverity(getSeverity(constraint.getSeverity(), ruleConfiguration.getDefaultConstraintSeverity()));
            writeExecutable(constraintType, constraint);
            writeRequiredConcepts(constraint, constraintType);
            rules.getConceptOrConstraintOrGroup().add(constraintType);
        }
    }

    private void writeRequiredConcepts(ExecutableRule<?> rule, ExecutableRuleType ruleType) {
        for (Map.Entry<String, Boolean> entry : rule.getRequiresConcepts().entrySet()) {
            ReferenceType conceptReferenceType = new ReferenceType();
            conceptReferenceType.setRefId(entry.getKey());
            conceptReferenceType.setOptional(entry.getValue());
            ruleType.getRequiresConcept().add(conceptReferenceType);
        }
    }

    private void writeExecutable(ExecutableRuleType executableRuleType, ExecutableRule executableRule) {
        Executable<?> executable = executableRule.getExecutable();
        SourceType sourceType = new SourceType();
        sourceType.setLanguage(executable.getLanguage());
        sourceType.setValue(executable.getSource().toString());
        executableRuleType.setSource(sourceType);
    }

    /**
     * Converts {@link Severity} to {@link SeverityEnumType}
     *
     * @param severity
     *            {@link Severity}, can be <code>null</code>
     * @param defaultSeverity
     *            default severity level, can be <code>null</code>
     * @return {@link SeverityEnumType}
     */
    private SeverityEnumType getSeverity(Severity severity, Severity defaultSeverity) {
        if (severity == null) {
            severity = defaultSeverity;
        }
        return defaultSeverity != null ? SeverityEnumType.fromValue(severity.getValue()) : null;
    }
}
