package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.validation.Schema;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSourceReaderPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;
import com.buschmais.jqassistant.core.rule.schema.v1.*;
import com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link RuleSourceReaderPlugin} implementation.
 */
public class XmlRuleSourceReaderPlugin implements RuleSourceReaderPlugin {

    private static final String NAMESPACE_RULES_1_0 = "http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0";
    private static final String NAMESPACE_RULES_1_1 = "http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.1";
    private static final String NAMESPACE_RULES_1_2 = "http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.2";
    private static final String NAMESPACE_RULES_1_3 = "http://www.buschmais.com/jqassistant/core/rule/schema/v1.3";
    private static final String NAMESPACE_RULES_1_4 = "http://www.buschmais.com/jqassistant/core/rule/schema/v1.4";
    private static final String RULES_SCHEMA_LOCATION = "/META-INF/xsd/jqassistant-rules-1.4.xsd";

    private static final Schema SCHEMA = XmlHelper.getSchema(RULES_SCHEMA_LOCATION);

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlRuleSourceReaderPlugin.class);

    private RuleConfiguration ruleConfiguration;

    private JAXBUnmarshaller<JqassistantRules> jaxbUnmarshaller;

    @Override
    public void initialize() {
        Map<String, String> namespaceMappings = new HashMap<>();
        namespaceMappings.put(NAMESPACE_RULES_1_0, NAMESPACE_RULES_1_4);
        namespaceMappings.put(NAMESPACE_RULES_1_1, NAMESPACE_RULES_1_4);
        namespaceMappings.put(NAMESPACE_RULES_1_2, NAMESPACE_RULES_1_4);
        namespaceMappings.put(NAMESPACE_RULES_1_3, NAMESPACE_RULES_1_4);
        this.jaxbUnmarshaller = new JAXBUnmarshaller<>(JqassistantRules.class, SCHEMA, namespaceMappings);
    }

    @Override
    public void configure(RuleConfiguration ruleConfiguration) {
        this.ruleConfiguration = ruleConfiguration;
    }

    @Override
    public boolean accepts(RuleSource ruleSource) {
        return ruleSource.getId().toLowerCase().endsWith(".xml");
    }

    @Override
    public void read(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException {
        List<JqassistantRules> rules = readXmlSource(ruleSource);
        convert(rules, ruleSource, ruleSetBuilder);
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
     * @param rules
     *            The {@link JqassistantRules}.
     * @throws RuleException
     *             If rules are not consistent.
     */
    private void convert(List<JqassistantRules> rules, RuleSource ruleSource, RuleSetBuilder builder) throws RuleException {
        for (JqassistantRules rule : rules) {
            List<SeverityRuleType> severityRuleTypes = rule.getConceptOrConstraintOrGroup();
            for (SeverityRuleType severityRuleType : severityRuleTypes) {
                String id = severityRuleType.getId();
                if (severityRuleType instanceof ConceptType) {
                    Concept concept = createConcept(id, ruleSource, (ConceptType) severityRuleType);
                    builder.addConcept(concept);
                } else if (severityRuleType instanceof ConstraintType) {
                    Constraint constraint = createConstraint(id, ruleSource, (ConstraintType) severityRuleType);
                    builder.addConstraint(constraint);
                } else if (severityRuleType instanceof GroupType) {
                    Group group = createGroup(id, ruleSource, (GroupType) severityRuleType);
                    builder.addGroup(group);
                }
            }
        }
    }

    private Group createGroup(String id, RuleSource ruleSource, GroupType referenceableType) throws RuleException {
        SeverityEnumType severityType = referenceableType.getSeverity();
        Severity severity = getSeverity(severityType, ruleConfiguration.getDefaultGroupSeverity());
        Map<String, Severity> includeConcepts = getIncludedReferences(referenceableType.getIncludeConcept());
        Map<String, Severity> includeConstraints = getIncludedReferences(referenceableType.getIncludeConstraint());
        Map<String, Severity> includeGroups = getIncludedReferences(referenceableType.getIncludeGroup());
        return Group.builder().id(id).severity(severity).ruleSource(ruleSource).conceptIds(includeConcepts).constraintIds(includeConstraints)
                .groupIds(includeGroups).build();
    }

    private Concept createConcept(String id, RuleSource ruleSource, ConceptType referenceableType) throws RuleException {
        String description = referenceableType.getDescription();
        Executable executable = createExecutable(referenceableType);
        Map<String, Parameter> parameters = getRequiredParameters(referenceableType.getRequiresParameter());
        SeverityEnumType severityType = referenceableType.getSeverity();
        Severity severity = getSeverity(severityType, ruleConfiguration.getDefaultConceptSeverity());
        List<ReferenceType> requiresConcept = referenceableType.getRequiresConcept();
        Map<String, Boolean> requiresConcepts = getRequiresConcepts(requiresConcept);
        String deprecated = referenceableType.getDeprecated();
        Verification verification = getVerification(referenceableType.getVerify());
        Report report = getReport(referenceableType.getReport());
        return Concept.builder().id(id).description(description).ruleSource(ruleSource).severity(severity).deprecation(deprecated).executable(executable)
                .parameters(parameters).requiresConceptIds(requiresConcepts).verification(verification).report(report).build();
    }

    /**
     * Read the report definition.
     *
     * @param reportType
     *            The report type.
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
        Report.Builder reportBuilder = Report.Builder.newInstance().primaryColumn(primaryColumn).properties(properties);
        if (type != null) {
            reportBuilder.selectTypes(type);
        }
        return reportBuilder.get();
    }

    private Constraint createConstraint(String id, RuleSource ruleSource, ConstraintType referenceableType) throws RuleException {
        Executable<?> executable = createExecutable(referenceableType);
        String description = referenceableType.getDescription();
        Map<String, Parameter> parameters = getRequiredParameters(referenceableType.getRequiresParameter());
        SeverityEnumType severityType = referenceableType.getSeverity();
        Severity severity = getSeverity(severityType, ruleConfiguration.getDefaultConstraintSeverity());
        List<ReferenceType> requiresConcept = referenceableType.getRequiresConcept();
        Map<String, Boolean> requiresConcepts = getRequiresConcepts(requiresConcept);
        String deprecated = referenceableType.getDeprecated();
        Verification verification = getVerification(referenceableType.getVerify());
        Report report = getReport(referenceableType.getReport());
        return Constraint.builder().id(id).description(description).ruleSource(ruleSource).severity(severity).deprecation(deprecated).executable(executable)
                .parameters(parameters).requiresConceptIds(requiresConcepts).verification(verification).report(report).build();
    }

    private Executable<?> createExecutable(ExecutableRuleType executableRuleType) throws RuleException {
        SourceType source = executableRuleType.getSource();
        if (source != null) {
            return new SourceExecutable<>(source.getLanguage().toLowerCase(), source.getValue());
        }
        // for compatibility
        String cypher = executableRuleType.getCypher();
        if (cypher != null) {
            return new CypherExecutable(cypher);
        }
        SourceType scriptType = executableRuleType.getScript();
        if (scriptType != null) {
            return new ScriptExecutable(scriptType.getLanguage().toLowerCase(), scriptType.getValue());
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
            if (aggregationVerificationType != null) {
                return AggregationVerification.builder().column(aggregationVerificationType.getColumn()).min(aggregationVerificationType.getMin())
                        .max(aggregationVerificationType.getMax()).build();
            } else if (rowCountVerificationType != null) {
                return RowCountVerification.builder().min(rowCountVerificationType.getMin()).max(rowCountVerificationType.getMax()).build();
            } else {
                throw new RuleException("Unsupported verification " + verificationType);
            }
        }
        return null;
    }

    private Map<String, Boolean> getRequiresConcepts(List<? extends ReferenceType> referenceTypes) {
        Map<String, Boolean> references = new HashMap<>();
        for (ReferenceType referenceType : referenceTypes) {
            references.put(referenceType.getRefId(), referenceType.isOptional());
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

    private Map<String, Parameter> getRequiredParameters(List<ParameterType> parameterTypes) throws RuleException {
        Map<String, Parameter> parameters = new HashMap<>();
        for (ParameterType parameterType : parameterTypes) {
            Parameter.Type type;
            switch (parameterType.getType()) {
            case CHAR:
                type = Parameter.Type.CHAR;
                break;
            case BYTE:
                type = Parameter.Type.BYTE;
                break;
            case SHORT:
                type = Parameter.Type.SHORT;
                break;
            case INT:
                type = Parameter.Type.INT;
                break;
            case LONG:
                type = Parameter.Type.LONG;
                break;
            case FLOAT:
                type = Parameter.Type.FLOAT;
                break;
            case DOUBLE:
                type = Parameter.Type.DOUBLE;
                break;
            case BOOLEAN:
                type = Parameter.Type.BOOLEAN;
                break;
            case STRING:
                type = Parameter.Type.STRING;
                break;
            default:
                throw new RuleException("Unsupported type " + parameterType.getType() + " of parameter " + parameterType.getName());
            }
            String defaultValue = parameterType.getDefaultValue();
            Parameter parameter = new Parameter(parameterType.getName(), type, defaultValue != null ? type.parse(defaultValue) : null);
            parameters.put(parameterType.getName(), parameter);
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
    private Severity getSeverity(SeverityEnumType severityType, Severity defaultSeverity) throws RuleException {
        return severityType == null ? defaultSeverity : Severity.fromValue(severityType.value());
    }
}
