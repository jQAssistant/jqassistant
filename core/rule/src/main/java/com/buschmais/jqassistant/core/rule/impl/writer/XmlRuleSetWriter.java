package com.buschmais.jqassistant.core.rule.impl.writer;

import java.io.Writer;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.executor.CollectRulesVisitor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.writer.RuleSetWriter;
import com.buschmais.jqassistant.core.rule.impl.reader.CDataXMLStreamWriter;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import org.jqassistant.schema.rule.v2.*;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of a {@link RuleSetWriter}.
 */
public class XmlRuleSetWriter implements RuleSetWriter {

    private JAXBContext jaxbContext;

    private Rule rule;

    public XmlRuleSetWriter(Rule rule) {
        this.rule = rule;
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    @Override
    public void write(RuleSet ruleSet, Writer writer) throws RuleException {
        CollectRulesVisitor visitor = new CollectRulesVisitor();
        RuleSelection ruleSelection = RuleSelection.builder()
            .groupIds(ruleSet.getGroupsBucket()
                .getIds())
            .constraintIds(ruleSet.getConstraintBucket()
                .getIds())
            .conceptIds(ruleSet.getConceptBucket()
                .getIds())
            .build();
        new RuleSetExecutor(visitor, rule).execute(ruleSet, ruleSelection);
        JqassistantRules rules = new JqassistantRules();
        writeGroups(visitor.getGroups(), rules);
        writeConcepts(visitor.getConcepts()
            .keySet(), rules);
        writeConstraints(visitor.getConstraints()
            .keySet(), rules);
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
            // Create map with providing concepts as keys and set of provided concept ids as values
            Map<String, Set<String>> providedConcepts = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : group.getProvidedConcepts()
                .entrySet()) {
                String providedConceptId = entry.getKey();
                for (String providingConceptId : entry.getValue()) {
                    providedConcepts.computeIfAbsent(providedConceptId, id -> new LinkedHashSet<>())
                        .add(providingConceptId);
                }
            }
            GroupType groupType = new GroupType();
            groupType.setId(group.getId());
            for (Map.Entry<String, Severity> groupEntry : group.getGroups()
                .entrySet()) {
                IncludedReferenceType groupReferenceType = new IncludedReferenceType();
                groupReferenceType.setRefId(groupEntry.getKey());
                groupType.setSeverity(getSeverity(groupEntry.getValue()));
                groupType.getIncludeGroup()
                    .add(groupReferenceType);
            }
            for (Map.Entry<String, Severity> conceptEntry : group.getConcepts()
                .entrySet()) {
                IncludeConceptType includeConceptType = new IncludeConceptType();
                includeConceptType.setRefId(conceptEntry.getKey());
                includeConceptType.setSeverity(getSeverity(conceptEntry.getValue()));
                for (String providedConceptId : providedConcepts.getOrDefault(conceptEntry.getKey(), Collections.emptySet())) {
                    ReferenceType referenceType = new ReferenceType();
                    referenceType.setRefId(providedConceptId);
                    includeConceptType.getProvidesConcept()
                        .add(referenceType);
                }
                groupType.getIncludeConcept()
                    .add(includeConceptType);
            }
            for (Map.Entry<String, Severity> constraintEntry : group.getConstraints()
                .entrySet()) {
                IncludedReferenceType constraintReferenceType = new IncludedReferenceType();
                constraintReferenceType.setRefId(constraintEntry.getKey());
                constraintReferenceType.setSeverity(getSeverity(constraintEntry.getValue()));
                groupType.getIncludeConstraint()
                    .add(constraintReferenceType);
            }
            rules.getConceptOrConstraintOrGroup()
                .add(groupType);
        }
    }

    private void writeConcepts(Collection<Concept> concepts, JqassistantRules rules) {
        for (Concept concept : concepts) {
            ConceptType conceptType = new ConceptType();
            conceptType.setId(concept.getId());
            conceptType.setDescription(concept.getDescription());
            conceptType.setSeverity(getSeverity(concept.getSeverity()));
            conceptType.setSource(writeExecutable(concept));
            conceptType.getRequiresConcept()
                .addAll(writeRequiredConcepts(concept));
            writeProvidedConcepts(concept, conceptType);
            rules.getConceptOrConstraintOrGroup()
                .add(conceptType);
        }
    }

    private void writeConstraints(Collection<Constraint> constraints, JqassistantRules rules) {
        for (Constraint constraint : constraints) {
            ConstraintType constraintType = new ConstraintType();
            constraintType.setId(constraint.getId());
            constraintType.setDescription(constraint.getDescription());
            constraintType.setSeverity(getSeverity(constraint.getSeverity()));
            constraintType.setSource(writeExecutable(constraint));
            constraintType.getRequiresConcept()
                .addAll(writeRequiredConcepts(constraint));
            rules.getConceptOrConstraintOrGroup()
                .add(constraintType);
        }
    }

    private List<OptionalReferenceType> writeRequiredConcepts(ExecutableRule<?> rule) {
        return rule.getRequiresConcepts()
            .entrySet()
            .stream()
            .map(entry -> {
                OptionalReferenceType conceptReferenceType = new OptionalReferenceType();
                conceptReferenceType.setRefId(entry.getKey());
                conceptReferenceType.setOptional(entry.getValue());
                return conceptReferenceType;
            })
            .collect(toList());
    }

    private void writeProvidedConcepts(Concept concept, ConceptType conceptType) {
        for (String refId : concept.getProvidedConcepts()) {
            ReferenceType conceptReferenceType = new ReferenceType();
            conceptReferenceType.setRefId(refId);
            conceptType.getProvidesConcept()
                .add(conceptReferenceType);
        }
    }

    private SourceType writeExecutable(ExecutableRule<?> executableRule) {
        Executable<?> executable = executableRule.getExecutable();
        if (executable != null) {
            SourceType sourceType = new SourceType();
            sourceType.setLanguage(executable.getLanguage());
            sourceType.setValue(executable.getSource()
                .toString());
            return sourceType;
        }
        return null;
    }

    /**
     * Converts {@link Severity} to {@link SeverityEnumType}
     *
     * @param severity
     *     {@link Severity}, can be <code>null</code>
     * @return {@link SeverityEnumType}
     */
    private SeverityEnumType getSeverity(Severity severity) {
        return severity != null ? SeverityEnumType.fromValue(severity.getValue()) : null;
    }
}
