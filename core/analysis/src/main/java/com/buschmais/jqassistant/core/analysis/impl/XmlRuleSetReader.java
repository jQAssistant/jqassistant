package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.*;
import com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.RuleSetReader} implementation.
 */
public class XmlRuleSetReader implements RuleSetReader {

    public static final String RULES_SCHEMA_LOCATION = "/META-INF/xsd/jqassistant-rules-1.1.xsd";
    public static final Schema SCHEMA = XmlHelper.getSchema(RULES_SCHEMA_LOCATION);

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlRuleSetReader.class);

    private JAXBUnmarshaller<JqassistantRules> jaxbUnmarshaller;

    public static final RowCountVerification DEFAULT_VERIFICATION = new RowCountVerification();

    public XmlRuleSetReader() {
        Map<String, String> namespaceMappings = new HashMap<>();
        namespaceMappings.put("http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0", "http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.1");
        this.jaxbUnmarshaller = new JAXBUnmarshaller<>(JqassistantRules.class, SCHEMA, namespaceMappings);
    }

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
     * @param ruleSource The available sources.
     * @return The list of found rules.
     */
    private List<JqassistantRules> readXmlSource(RuleSource ruleSource) {
        List<JqassistantRules> rules = new ArrayList<>();
        try (InputStream inputStream = ruleSource.getInputStream()) {
            LOGGER.debug("Reading rules from '{}'.", ruleSource.getId());
            JqassistantRules jqassistantRules = jaxbUnmarshaller.unmarshal(inputStream);
            rules.add(jqassistantRules);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read rules from '" + ruleSource.getId() + "'.", e);
        }
        return rules;
    }

    /**
     * Converts a list of {@link JqassistantRules} to a rule set.
     *
     * @param rules The {@link JqassistantRules}.
     * @throws com.buschmais.jqassistant.core.analysis.api.RuleException If rules are not consistent.
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
        return Template.Builder.newTemplate().id(templateType.getId()).description(templateType.getDescription()).ruleSource(ruleSource).executable(executable).parameterTypes(parameterTypes).get();
    }

    private MetricGroup createMetricGroup(String id, RuleSource ruleSource, MetricGroupType referenceableType) {
        Map<String, Metric> metrics = new LinkedHashMap<>();
        for (MetricType metricType : referenceableType.getMetric()) {
            String cypher = metricType.getCypher();
            String description = metricType.getDescription();
            Map<String, Class<?>> parameterTypes = getParameterTypes(metricType.getParameterDefinition());
            Set<String> requiresConcepts = getRequiredReferences(metricType.getRequiresConcept());
            Metric metric =
                    Metric.Builder.newMetric().id(id).description(description).ruleSource(ruleSource).executable(new CypherExecutable(cypher)).parameterTypes(parameterTypes).requiresConceptIds(requiresConcepts).get();
            metrics.put(metricType.getId(), metric);
        }
        return MetricGroup.Builder.newMetricGroup().id(id).description(referenceableType.getDescription()).ruleSource(ruleSource).metrics(metrics).get();
    }

    private Group createGroup(String id, RuleSource ruleSource, GroupType referenceableType) throws RuleException {
        SeverityEnumType severityType = referenceableType.getSeverity();
        Severity severity = getSeverity(severityType, Group.DEFAULT_SEVERITY);
        Map<String, Severity> includeConcepts = getIncludedReferences(referenceableType.getIncludeConcept());
        Map<String, Severity> includeConstraints = getIncludedReferences(referenceableType.getIncludeConstraint());
        Map<String, Severity> includeGroups = getIncludedReferences(referenceableType.getIncludeGroup());
        return Group.Builder.newGroup().id(id).severity(severity).ruleSource(ruleSource).conceptIds(includeConcepts).constraintIds(includeConstraints).groupIds(includeGroups).get();
    }

    private Concept createConcept(String id, RuleSource ruleSource, ConceptType referenceableType) throws RuleException {
        String description = referenceableType.getDescription();
        Executable executable = createExecutable(referenceableType);
        Map<String, Object> parameters = getParameterValues(referenceableType.getParameter());
        SeverityEnumType severityType = referenceableType.getSeverity();
        Severity severity = getSeverity(severityType, Concept.DEFAULT_SEVERITY);
        List<ReferenceType> requiresConcept = referenceableType.getRequiresConcept();
        Set<String> requiresConcepts = getRequiredReferences(requiresConcept);
        String deprecated = referenceableType.getDeprecated();
        Verification verification = getVerification(referenceableType.getVerify());
        Report report = getReport(referenceableType.getReport());
        return Concept.Builder.newConcept().id(id).description(description).ruleSource(ruleSource).severity(severity).deprecation(deprecated).executable(executable).parameters(parameters).requiresConceptIds(requiresConcepts).verification(verification).report(report).get();
    }

    /**
     * Read the report definition.
     *
     * @param reportType The report type.
     * @return The report definition.
     */
    private Report getReport(ReportType reportType) {
        String type = null;
        String primaryColumn = null;
        Properties properties = new Properties();
        if (reportType != null) {
            type = reportType.getType();
            primaryColumn = reportType.getPrimaryColumn();
            for (PropertyType propertyType : reportType.getProperty()) {
                properties.setProperty(propertyType.getName(), propertyType.getValue());
            }
        }
        return new Report(type != null ? type : null, primaryColumn, properties);
    }

    private Constraint createConstraint(String id, RuleSource ruleSource, ConstraintType referenceableType) throws RuleException {
        Executable executable = createExecutable(referenceableType);
        String description = referenceableType.getDescription();
        Map<String, Object> parameters = getParameterValues(referenceableType.getParameter());
        SeverityEnumType severityType = referenceableType.getSeverity();
        Severity severity = getSeverity(severityType, Constraint.DEFAULT_SEVERITY);
        List<ReferenceType> requiresConcept = referenceableType.getRequiresConcept();
        Set<String> requiresConcepts = getRequiredReferences(requiresConcept);
        String deprecated = referenceableType.getDeprecated();
        Verification verification = getVerification(referenceableType.getVerify());
        Report report = getReport(referenceableType.getReport());
        return Constraint.Builder.newConstraint().id(id).description(description).ruleSource(ruleSource).severity(severity).deprecation(deprecated).executable(executable).parameters(parameters).requiresConceptIds(requiresConcepts).verification(verification).report(report).get();
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

    private Set<String> getRequiredReferences(List<? extends ReferenceType> referenceTypes) {
        Set<String> references = new HashSet<>();
        for (ReferenceType referenceType : referenceTypes) {
            references.add(referenceType.getRefId());
        }
        return references;
    }

    private Map<String, Severity> getIncludedReferences(List<IncludedReferenceType> referenceType) throws RuleException {
        Map<String, Severity> references = new HashMap<>();
        for (IncludedReferenceType includedReferenceType : referenceType) {
            Severity severity = getSeverity(includedReferenceType.getSeverity(), null);
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
     * @param severityType    The severity type.
     * @param defaultSeverity The default severity.
     * @return The severity.
     */
    private Severity getSeverity(SeverityEnumType severityType, Severity defaultSeverity) throws RuleException {
        return severityType == null ? defaultSeverity : Severity.fromValue(severityType.value());
    }

    /**
     * Get a map of parameters.
     *
     * @param parameter The parameters.
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
     * @param type        The {@link ParameterType}.
     * @param stringValue The string representation.
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
