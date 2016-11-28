package com.buschmais.jqassistant.core.analysis.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.*;
import com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.RuleSetReader}
 * implementation.
 */
public class XmlRuleSetReader implements RuleSetReader {

    public static final String RULES_SCHEMA_LOCATION = "/META-INF/xsd/jqassistant-rules-1.2.xsd";
    public static final Schema SCHEMA = XmlHelper.getSchema(RULES_SCHEMA_LOCATION);

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlRuleSetReader.class);

    private JAXBUnmarshaller<JqassistantRules> jaxbUnmarshaller;

    public static final RowCountVerification DEFAULT_VERIFICATION = new RowCountVerification();

    public XmlRuleSetReader() {
        Map<String, String> namespaceMappings = new HashMap<>();
        namespaceMappings.put("http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.0",
                "http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.2");
        namespaceMappings.put("http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.1",
                "http://www.buschmais.com/jqassistant/core/analysis/rules/schema/v1.2");
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
     * @throws com.buschmais.jqassistant.core.analysis.api.RuleException
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
        Severity severity = getSeverity(severityType, Group.DEFAULT_SEVERITY);
        Map<String, Severity> includeConcepts = getIncludedReferences(referenceableType.getIncludeConcept());
        Map<String, Severity> includeConstraints = getIncludedReferences(referenceableType.getIncludeConstraint());
        Map<String, Severity> includeGroups = getIncludedReferences(referenceableType.getIncludeGroup());
        return Group.Builder.newGroup().id(id).severity(severity).ruleSource(ruleSource).conceptIds(includeConcepts).constraintIds(includeConstraints)
                .groupIds(includeGroups).get();
    }

    private Concept createConcept(String id, RuleSource ruleSource, ConceptType referenceableType) throws RuleException {
        String description = referenceableType.getDescription();
        Executable executable = createExecutable(referenceableType);
        Map<String, Parameter> parameters = getRequiredParameters(referenceableType.getRequiresParameter());
        SeverityEnumType severityType = referenceableType.getSeverity();
        Severity severity = getSeverity(severityType, Concept.DEFAULT_SEVERITY);
        List<ReferenceType> requiresConcept = referenceableType.getRequiresConcept();
        Set<String> requiresConcepts = getRequiredReferences(requiresConcept);
        String deprecated = referenceableType.getDeprecated();
        Verification verification = getVerification(referenceableType.getVerify());
        Report report = getReport(referenceableType.getReport());
        return Concept.Builder.newConcept().id(id).description(description).ruleSource(ruleSource).severity(severity).deprecation(deprecated)
                .executable(executable).parameters(parameters).requiresConceptIds(requiresConcepts).verification(verification).report(report).get();
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
        Executable executable = createExecutable(referenceableType);
        String description = referenceableType.getDescription();
        Map<String, Parameter> parameters = getRequiredParameters(referenceableType.getRequiresParameter());
        SeverityEnumType severityType = referenceableType.getSeverity();
        Severity severity = getSeverity(severityType, Constraint.DEFAULT_SEVERITY);
        List<ReferenceType> requiresConcept = referenceableType.getRequiresConcept();
        Set<String> requiresConcepts = getRequiredReferences(requiresConcept);
        String deprecated = referenceableType.getDeprecated();
        Verification verification = getVerification(referenceableType.getVerify());
        Report report = getReport(referenceableType.getReport());
        return Constraint.Builder.newConstraint().id(id).description(description).ruleSource(ruleSource).severity(severity).deprecation(deprecated)
                .executable(executable).parameters(parameters).requiresConceptIds(requiresConcepts).verification(verification).report(report).get();
    }

    private Executable createExecutable(ExecutableRuleType executableRuleType) throws RuleException {
        String cypher = executableRuleType.getCypher();
        ScriptType scriptType = executableRuleType.getScript();
        if (cypher != null) {
            return new CypherExecutable(cypher);
        } else if (scriptType != null) {
            return new ScriptExecutable(scriptType.getLanguage(), scriptType.getValue());
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

    private Map<String, Parameter> getRequiredParameters(List<ParameterType> parameterTypes) throws RuleException {
        Map<String, Parameter> parameters = new HashMap<>();
        for (ParameterType parameterType : parameterTypes) {
            Parameter.Type type;
            switch (parameterType.getType()) {
            case SHORT:
                type = Parameter.Type.SHORT;
                break;
            case INT:
                type = Parameter.Type.INT;
                break;
            case LONG:
                type = Parameter.Type.LONG;
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
