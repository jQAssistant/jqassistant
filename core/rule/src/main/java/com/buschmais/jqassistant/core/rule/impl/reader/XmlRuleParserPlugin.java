package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.validation.Schema;

import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;
import com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller;

import org.jqassistant.schema.rule.v1.*;

import static java.util.stream.Collectors.toSet;

/**
 * A {@link RuleParserPlugin} implementation.
 */
public class XmlRuleParserPlugin extends AbstractRuleParserPlugin {

    private static final String NAMESPACE_RULE_1_10 = "http://schema.jqassistant.org/rule/v1.10";
    private static final String RULES_SCHEMA_LOCATION = "/META-INF/rule/xsd/jqassistant-rule-v1.10.xsd";

    private static final Schema SCHEMA = XmlHelper.getSchema(RULES_SCHEMA_LOCATION);

    private JAXBUnmarshaller<JqassistantRules> jaxbUnmarshaller;

    @Override
    public void initialize() {
        this.jaxbUnmarshaller = new JAXBUnmarshaller<>(JqassistantRules.class, SCHEMA, NAMESPACE_RULE_1_10);
    }

    @Override
    public boolean accepts(RuleSource ruleSource) {
        return ruleSource.getId().toLowerCase().endsWith(".xml");
    }

    @Override
    public void doParse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException {
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
        Severity severity = getSeverity(severityType, getRuleConfiguration().getDefaultGroupSeverity());
        Map<String, Severity> includeConcepts = getIncludedReferences(referenceableType.getIncludeConcept());
        Map<String, Severity> includeConstraints = getIncludedReferences(referenceableType.getIncludeConstraint());
        Map<String, Severity> includeGroups = getIncludedReferences(referenceableType.getIncludeGroup());
        return Group.builder().id(id).severity(severity).ruleSource(ruleSource).concepts(includeConcepts).constraints(includeConstraints).groups(includeGroups)
                .build();
    }

    private Concept createConcept(String id, RuleSource ruleSource, ConceptType conceptType) throws RuleException {
        String description = conceptType.getDescription();
        Executable executable = createExecutable(conceptType, conceptType.getSource(), conceptType.getCypher(), conceptType.getScript());
        Map<String, Parameter> parameters = getRequiredParameters(conceptType.getRequiresParameter());
        SeverityEnumType severityType = conceptType.getSeverity();
        Severity severity = getSeverity(severityType, getRuleConfiguration().getDefaultConceptSeverity());
        List<OptionalReferenceType> requiresConcept = conceptType.getRequiresConcept();
        Map<String, Boolean> requiresConcepts = getRequiresConcepts(requiresConcept);
        List<ReferenceType> providesConcept = conceptType.getProvidesConcept();
        Set<String> providesConcepts = providesConcept.stream().map(referenceType -> referenceType.getRefId()).collect(toSet());
        String deprecated = conceptType.getDeprecated();
        Verification verification = getVerification(conceptType.getVerify());
        Report report = getReport(conceptType.getReport());
        return Concept.builder().id(id).description(description).ruleSource(ruleSource).severity(severity).deprecation(deprecated).executable(executable)
                .parameters(parameters).providesConcepts(providesConcepts).requiresConcepts(requiresConcepts).verification(verification).report(report).build();
    }

    private Constraint createConstraint(String id, RuleSource ruleSource, ConstraintType constraintType) throws RuleException {
        Executable<?> executable = createExecutable(constraintType, constraintType.getSource(), constraintType.getCypher(), constraintType.getScript());
        String description = constraintType.getDescription();
        Map<String, Parameter> parameters = getRequiredParameters(constraintType.getRequiresParameter());
        SeverityEnumType severityType = constraintType.getSeverity();
        Severity severity = getSeverity(severityType, getRuleConfiguration().getDefaultConstraintSeverity());
        List<OptionalReferenceType> requiresConcept = constraintType.getRequiresConcept();
        Map<String, Boolean> requiresConcepts = getRequiresConcepts(requiresConcept);
        String deprecated = constraintType.getDeprecated();
        Verification verification = getVerification(constraintType.getVerify());
        Report report = getReport(constraintType.getReport());
        return Constraint.builder().id(id).description(description).ruleSource(ruleSource).severity(severity).deprecation(deprecated).executable(executable)
                .parameters(parameters).requiresConcepts(requiresConcepts).verification(verification).report(report).build();
    }

    private Executable<?> createExecutable(SeverityRuleType severityRuleType, SourceType source, String cypher, SourceType scriptType) throws RuleException {
        if (source != null) {
            return new SourceExecutable<>(source.getLanguage().toLowerCase(), source.getValue(), String.class);
        }
        // for compatibility
        if (cypher != null) {
            return new CypherExecutable(cypher);
        }
        if (scriptType != null) {
            return new ScriptExecutable(scriptType.getLanguage().toLowerCase(), scriptType.getValue());
        }
        throw new RuleException("Cannot determine executable for " + severityRuleType.getId());
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
        Report.ReportBuilder reportBuilder = Report.builder().primaryColumn(primaryColumn).properties(properties);
        if (type != null) {
            reportBuilder.selectedTypes(Report.selectTypes(type));
        }
        return reportBuilder.build();
    }

    private Map<String, Boolean> getRequiresConcepts(List<? extends OptionalReferenceType> referenceTypes) {
        Map<String, Boolean> references = new HashMap<>();
        for (OptionalReferenceType referenceType : referenceTypes) {
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
