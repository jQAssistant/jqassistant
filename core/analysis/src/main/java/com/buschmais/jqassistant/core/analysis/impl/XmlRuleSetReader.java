package com.buschmais.jqassistant.core.analysis.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.*;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.RuleSetReader} implementation.
 */
public class XmlRuleSetReader implements RuleSetReader {

    public static final String RULES_SCHEMA_LOCATION = "/META-INF/xsd/jqassistant-rules-1.0.xsd";
    public static final Schema SCHEMA = XmlHelper.getSchema(RULES_SCHEMA_LOCATION);

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlRuleSetReader.class);

    private static final JAXBContext JAXB_CONTEXT;

    /**
     * Static constructor.
     */
    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    public static final RowCountVerification DEFAULT_VERIFICATION = new RowCountVerification();

    @Override
    public void read(List<? extends RuleSource> sources, RuleSetBuilder ruleSetBuilder) throws RuleException {
        for (RuleSource ruleSource : sources) {
            if (ruleSource.isType(RuleSource.Type.XML)) {
                List<JqassistantRules> rules = readXmlSource(ruleSource);
                convert(rules, ruleSource, ruleSetBuilder);
            }
        }
    }

    /**
     * Read rules from XML documents.
     * 
     * @param ruleSource
     *            The available sources.
     * @return The list of found rules.
     */
    private List<JqassistantRules> readXmlSource(RuleSource ruleSource) {
        List<JqassistantRules> rules = new ArrayList<>();
        try (InputStream inputStream = ruleSource.getInputStream()) {
            Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            unmarshaller.setSchema(SCHEMA);
            LOGGER.debug("Reading rules from '{}'.", ruleSource.getId());
            StreamSource streamSource = new StreamSource(inputStream);
            JAXBElement<JqassistantRules> jaxbElement = unmarshaller.unmarshal(streamSource, JqassistantRules.class);
            rules.add(jaxbElement.getValue());
        } catch (IOException | JAXBException e) {
            throw new IllegalArgumentException("Cannot read rules from '" + ruleSource.getId() + "'.", e);
        }
        return rules;
    }

    /**
     * Converts a list of {@link JqassistantRules} to a rule set.
     *
     * @param rules
     *            The {@link JqassistantRules}.
     *
     * @return The corresponding rule set.
     * @throws com.buschmais.jqassistant.core.analysis.api.RuleException
     *             If rules are not consistent.
     * 
     */
    private void convert(List<JqassistantRules> rules, RuleSource ruleSource, RuleSetBuilder builder) throws RuleException {
        for (JqassistantRules rule : rules) {
            List<ReferenceableType> queryDefinitionOrConceptOrConstraint = rule.getTemplateOrConceptOrConstraint();
            for (ReferenceableType referenceableType : queryDefinitionOrConceptOrConstraint) {
                String id = referenceableType.getId();
                if (referenceableType instanceof TemplateType) {
                    Template template = createTemplate((TemplateType) referenceableType, ruleSource);
                    builder.addTemplate(template);
                } else {
                    if (referenceableType instanceof ConceptType) {
                        Concept concept = createConcept(id, ruleSource, (ConceptType) referenceableType);
                        builder.addConcept(concept);
                    } else if (referenceableType instanceof ConstraintType) {
                        Constraint constraint = createConstraint(id, ruleSource, (ConstraintType) referenceableType);
                        builder.addConstraint(constraint);
                    } else if (referenceableType instanceof GroupType) {
                        Group group = createGroup(id, ruleSource, (GroupType) referenceableType);
                        builder.addGroup(group);
                    } else if (referenceableType instanceof MetricGroupType) {
                        MetricGroup metricGroup = createMetricGroup(id, ruleSource, (MetricGroupType) referenceableType);
                        builder.addMetricGroup(metricGroup);
                    }
                }
            }
        }
    }

    private Template createTemplate(TemplateType templateType, RuleSource ruleSource) throws RuleException {
        Map<String, Class<?>> parameterTypes = new HashMap<>();
        for (ParameterDefinitionType parameterDefinitionType : templateType.getParameterDefinition()) {
            Class<?> parameterType;
            switch (parameterDefinitionType.getType()) {
                case INT:
                    parameterType = Integer.class;
                    break;
                case STRING:
                    parameterType = String.class;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported parameter parameterDefinitionType " + parameterDefinitionType.getType());
            }
            parameterTypes.put(parameterDefinitionType.getName(), parameterType);
        }
        Executable executable = createExecutable(templateType);
        return new Template(templateType.getId(), templateType.getDescription(), ruleSource, executable, parameterTypes);
    }

    private MetricGroup createMetricGroup(String id, RuleSource ruleSource, MetricGroupType referenceableType) {
        MetricGroupType metricGroupType = referenceableType;
        Map<String, Metric> metrics = new LinkedHashMap<>();
        for (MetricType metricType : metricGroupType.getMetric()) {
            String cypher = metricType.getCypher();
            String description = metricType.getDescription();
            Map<String, Class<?>> parameterTypes = getParameterTypes(metricType.getParameterDefinition());
            Set<String> requiresConcepts = getReferences(metricType.getRequiresConcept());
            Metric metric = new Metric(metricType.getId(), description, ruleSource, new CypherExecutable(cypher), parameterTypes,
                    requiresConcepts);
            metrics.put(metricType.getId(), metric);
        }
        return new MetricGroup(id, metricGroupType.getDescription(), ruleSource, metrics);
    }

    private Group createGroup(String id, RuleSource ruleSource, GroupType referenceableType) {
        GroupType groupType = referenceableType;
        Map<String, Severity> includeConcepts = getReferences(groupType.getIncludeConcept(), Concept.DEFAULT_SEVERITY);
        Map<String, Severity> includeConstraints = getReferences(groupType.getIncludeConstraint(), Constraint.DEFAULT_SEVERITY);
        Set<String> includeGroups = getReferences(groupType.getIncludeGroup());
        return new Group(id, null, ruleSource, includeConcepts, includeConstraints, includeGroups);
    }

    private Concept createConcept(String id, RuleSource ruleSource, ConceptType referenceableType) throws RuleException {
        ConceptType conceptType = referenceableType;
        String description = conceptType.getDescription();
        Executable executable = createExecutable(referenceableType);
        Map<String, Object> parameters = getParameterValues(conceptType.getParameter());
        SeverityEnumType severityType = conceptType.getSeverity();
        Severity severity = getSeverity(severityType, Concept.DEFAULT_SEVERITY);
        List<ReferenceType> requiresConcept = conceptType.getRequiresConcept();
        Set<String> requiresConcepts = getReferences(requiresConcept);
        String deprecated = conceptType.getDeprecated();
        Verification verification = getVerification(conceptType.getVerify());
        Report report = getReport(conceptType.getReport());
        return new Concept(id, description, ruleSource, severity, deprecated, executable, parameters, requiresConcepts, verification, report);
    }

    /**
     * Read the report definition.
     * 
     * @param reportType
     *            The report type.
     * @return The report definition.
     */
    private Report getReport(ReportType reportType) {
        String primaryColumn = null;
        if (reportType != null) {
            primaryColumn = reportType.getPrimaryColumn();
        }
        return new Report(primaryColumn);
    }

    private Constraint createConstraint(String id, RuleSource ruleSource, ConstraintType referenceableType) throws RuleException {
        ConstraintType constraintType = referenceableType;
        Executable executable = createExecutable(constraintType);
        String description = constraintType.getDescription();
        Map<String, Object> parameters = getParameterValues(constraintType.getParameter());
        SeverityEnumType severityType = constraintType.getSeverity();
        Severity severity = getSeverity(severityType, Constraint.DEFAULT_SEVERITY);
        List<ReferenceType> requiresConcept = constraintType.getRequiresConcept();
        Set<String> requiresConcepts = getReferences(requiresConcept);
        String deprecated = constraintType.getDeprecated();
        Verification verification = getVerification(constraintType.getVerify());
        Report report = getReport(constraintType.getReport());
        return new Constraint(id, description, ruleSource, severity, deprecated, executable, parameters, requiresConcepts, verification, report);
    }

    private Executable createExecutable(ExecutableRuleType executableRuleType) throws RuleException {
        String cypher = executableRuleType.getCypher();
        ScriptType scriptType = executableRuleType.getScript();
        ReferenceType useTemplate = executableRuleType.getUseTemplate();
        if (cypher != null) {
            return new CypherExecutable(cypher);
        } else if (scriptType != null) {
            return new ScriptExecutable(scriptType.getLanguage(), scriptType.getValue());
        } else if (useTemplate != null) {
            return new TemplateExecutable(useTemplate.getRefId());
        }
        throw new RuleException("Cannot determine executable for " + executableRuleType.getId());
    }

    /**
     * Read the verification definition.
     */
    private Verification getVerification(VerificationType verificationType) throws RuleException {
        if (verificationType != null) {
            RowCountVerificationType rowCountVerificationType = verificationType.getRowCount();
            AggregationVerificationType aggregationVerificationType = verificationType.getAggregation();
            if (rowCountVerificationType != null) {
                return new RowCountVerification();
            } else if (aggregationVerificationType != null) {
                String column = aggregationVerificationType.getColumn();
                return new AggregationVerification(column);
            } else {
                throw new RuleException("Unsupported verification " + verificationType);
            }
        }
        return DEFAULT_VERIFICATION;
    }

    private Set<String> getReferences(List<? extends ReferenceType> referenceTypes) {
        Set<String> references = new HashSet<>();
        for (ReferenceType referenceType : referenceTypes) {
            references.add(referenceType.getRefId());
        }
        return references;
    }

    private Map<String, Severity> getReferences(List<IncludedReferenceType> referenceType, Severity defaultSeverity) {
        Map<String, Severity> references = new HashMap<>();
        for (IncludedReferenceType includedReferenceType : referenceType) {
            Severity severity = getSeverity(includedReferenceType.getSeverity(), defaultSeverity);
            references.put(includedReferenceType.getRefId(), severity);
        }
        return references;
    }

    private Map<String, Class<?>> getParameterTypes(List<ParameterDefinitionType> parameterDefinitionTypes) {
        Map<String, Class<?>> parameters = new HashMap<>();
        for (ParameterDefinitionType parameterDefinitionType : parameterDefinitionTypes) {
            Class<?> type;
            switch (parameterDefinitionType.getType()) {
                case INT:
                    type = Integer.class;
                    break;
                case STRING:
                    type = String.class;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported parameter definition type" + parameterDefinitionType);
            }
            parameters.put(parameterDefinitionType.getName(), type);
        }
        return parameters;
    }

    /**
     * Get the severity.
     * 
     * @param severityType
     *            The severity type.
     * @param defaultSeverity
     *            The default severity.
     * @return The severity.
     */
    private Severity getSeverity(SeverityEnumType severityType, Severity defaultSeverity) {
        return severityType == null ? defaultSeverity : Severity.fromValue(severityType.value());
    }

    /**
     * Get a parameter value by its string representation and types.
     *
     * @param type
     *            The {@link ParameterType}.
     * @param stringValue
     *            The string representation.
     *
     * @return The parameter value.
     */
    private Object getParameterType(ParameterTypes type, String stringValue) {
        Object value;
        switch (type) {
            case INT:
                value = Integer.valueOf(stringValue);
                break;
            case STRING:
                value = stringValue;
                break;
            default:
                throw new IllegalArgumentException("Unsupported parameter types: " + type);
        }
        return value;
    }

    /**
     * Get a map of parameters.
     * 
     * @param parameter
     *            The parameters.
     * @return The map of parameters.
     */
    private Map<String, Object> getParameterValues(List<ParameterType> parameter) {
        Map<String, Object> parameters = new HashMap<>();
        for (ParameterType parameterType : parameter) {
            Object value = getParameterValue(parameterType.getType(), parameterType.getValue());
            parameters.put(parameterType.getName(), value);
        }
        return parameters;
    }

    /**
     * Get a parameter value by its string representation and types.
     *
     * @param type
     *            The {@link ParameterType}.
     * @param stringValue
     *            The string representation.
     *
     * @return The parameter value.
     */
    private Object getParameterValue(ParameterTypes type, String stringValue) {
        Object value;
        switch (type) {
            case INT:
                value = Integer.valueOf(stringValue);
                break;
            case STRING:
                value = stringValue;
                break;
            default:
                throw new IllegalArgumentException("Unsupported parameter types: " + type);
        }
        return value;
    }
}
