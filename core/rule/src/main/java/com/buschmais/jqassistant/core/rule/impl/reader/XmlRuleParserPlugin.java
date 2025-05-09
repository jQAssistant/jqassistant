package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import javax.xml.validation.Schema;

import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;
import com.buschmais.jqassistant.core.shared.xml.JAXBHelper;
import com.buschmais.jqassistant.core.shared.xml.XmlHelper;

import lombok.extern.slf4j.Slf4j;
import org.jqassistant.schema.rule.v2.*;

import static com.buschmais.jqassistant.core.rule.impl.reader.IndentHelper.removeIndent;
import static java.util.stream.Collectors.toSet;

/**
 * A {@link RuleParserPlugin} implementation.
 */
@Slf4j
public class XmlRuleParserPlugin extends AbstractRuleParserPlugin {

    private static final String NAMESPACE_RULE = "http://schema.jqassistant.org/rule/v2.7";
    private static final String RULES_SCHEMA_LOCATION = "/META-INF/schema/jqassistant-rule-v2.7.xsd";

    private static final Schema SCHEMA = XmlHelper.getSchema(RULES_SCHEMA_LOCATION);

    private JAXBHelper<JqassistantRules> jaxbHelper;

    @Override
    public void initialize() {
        this.jaxbHelper = new JAXBHelper<>(JqassistantRules.class, SCHEMA, NAMESPACE_RULE);
    }

    @Override
    public boolean accepts(RuleSource ruleSource) {
        return ruleSource.getId()
            .toLowerCase()
            .endsWith(".xml") && XmlHelper.rootElementMatches(ruleSource::getInputStream, qname -> "jqassistant-rules".equals(qname.getLocalPart()));
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
     *     The available sources.
     * @return The list of found rules.
     */
    private List<JqassistantRules> readXmlSource(RuleSource ruleSource) {
        List<JqassistantRules> rules = new ArrayList<>();
        try {
            JqassistantRules jqassistantRules = jaxbHelper.unmarshal(ruleSource.getURL());
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
     *     The {@link JqassistantRules}.
     * @throws RuleException
     *     If rules are not consistent.
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

    private Group createGroup(String id, RuleSource ruleSource, GroupType referencableType) throws RuleException {
        SeverityEnumType severityType = referencableType.getSeverity();
        Severity severity = getSeverity(severityType, this::getDefaultGroupSeverity);
        Map<String, Severity> includeConcepts = getIncludedReferences(referencableType.getIncludeConcept());
        Map<String, Set<Concept.ProvidedConcept>> providedConcepts = getProvidedConcepts(referencableType.getIncludeConcept());
        Map<String, Severity> includeConstraints = getIncludedReferences(referencableType.getIncludeConstraint());
        Map<String, Severity> includeGroups = getIncludedReferences(referencableType.getIncludeGroup());
        return Group.builder()
            .id(id)
            .severity(severity)
            .ruleSource(ruleSource)
            .concepts(includeConcepts)
            .providedConcepts(providedConcepts)
            .constraints(includeConstraints)
            .groups(includeGroups)
            .build();
    }

    private Concept createConcept(String id, RuleSource ruleSource, ConceptType conceptType) throws RuleException {
        String description = removeIndent(conceptType.getDescription());
        Executable<?> executable = createExecutable(conceptType.getSource(), conceptType.getCypher(), conceptType.getScript());
        Map<String, Parameter> parameters = getRequiredParameters(conceptType.getRequiresParameter());
        SeverityEnumType severityType = conceptType.getSeverity();
        Severity severity = getSeverity(severityType, this::getDefaultConceptSeverity);
        List<OptionalReferenceType> requiresConcept = conceptType.getRequiresConcept();
        Map<String, Boolean> requiresConcepts = getRequiresConcepts(requiresConcept);
        Set<Concept.ProvidedConcept> providedConcepts = conceptType.getProvidesConcept()
            .stream()
            .map(providesReferenceType -> Concept.ProvidedConcept.builder()
                .providingConceptId(id)
                .providedConceptId(providesReferenceType.getRefId())
                .activation(getActivation(providesReferenceType))
                .build())
            .collect(toSet());
        String deprecated = conceptType.getDeprecated();
        Verification verification = getVerification(conceptType.getVerify());
        Report report = getReport(conceptType.getReport());
        return Concept.builder()
            .id(id)
            .description(description)
            .ruleSource(ruleSource)
            .severity(severity)
            .deprecation(deprecated)
            .executable(executable)
            .parameters(parameters)
            .providedConcepts(providedConcepts)
            .requiresConcepts(requiresConcepts)
            .verification(verification)
            .report(report)
            .build();
    }

    private Constraint createConstraint(String id, RuleSource ruleSource, ConstraintType constraintType) throws RuleException {
        Executable<?> executable = createExecutable(constraintType.getSource(), constraintType.getCypher(), constraintType.getScript());
        String description = removeIndent(constraintType.getDescription());
        Map<String, Parameter> parameters = getRequiredParameters(constraintType.getRequiresParameter());
        SeverityEnumType severityType = constraintType.getSeverity();
        Severity severity = getSeverity(severityType, this::getDefaultConstraintSeverity);
        List<OptionalReferenceType> requiresConcept = constraintType.getRequiresConcept();
        Map<String, Boolean> requiresConcepts = getRequiresConcepts(requiresConcept);
        String deprecated = constraintType.getDeprecated();
        Verification verification = getVerification(constraintType.getVerify());
        Report report = getReport(constraintType.getReport());
        return Constraint.builder()
            .id(id)
            .description(description)
            .ruleSource(ruleSource)
            .severity(severity)
            .deprecation(deprecated)
            .executable(executable)
            .parameters(parameters)
            .requiresConcepts(requiresConcepts)
            .verification(verification)
            .report(report)
            .build();
    }

    private Executable<?> createExecutable(SourceType source, CypherType cypherType, SourceType scriptType) {
        if (source != null) {
            return new SourceExecutable<>(source.getLanguage()
                .toLowerCase(), source.getValue(), String.class);
        }
        // for compatibility
        if (cypherType != null) {
            return new CypherExecutable(cypherType.getValue());
        }
        if (scriptType != null) {
            return new ScriptExecutable(scriptType.getLanguage()
                .toLowerCase(), scriptType.getValue());
        }
        return null;
    }

    /**
     * Read the verification definition.
     */
    private Verification getVerification(VerificationType verificationType) throws RuleException {
        if (verificationType != null) {
            RowCountVerificationType rowCountVerificationType = verificationType.getRowCount();
            AggregationVerificationType aggregationVerificationType = verificationType.getAggregation();
            if (aggregationVerificationType != null) {
                return AggregationVerification.builder()
                    .column(aggregationVerificationType.getColumn())
                    .min(aggregationVerificationType.getMin())
                    .max(aggregationVerificationType.getMax())
                    .build();
            } else if (rowCountVerificationType != null) {
                return RowCountVerification.builder()
                    .min(rowCountVerificationType.getMin())
                    .max(rowCountVerificationType.getMax())
                    .build();
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
     *     The report type.
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
        Report.ReportBuilder reportBuilder = Report.builder()
            .primaryColumn(primaryColumn)
            .properties(properties);
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

    private Map<String, Set<Concept.ProvidedConcept>> getProvidedConcepts(List<IncludeConceptType> conceptTypes) {
        Map<String, Set<Concept.ProvidedConcept>> providedConcepts = new HashMap<>();
        for (IncludeConceptType conceptType : conceptTypes) {
            for (ProvidesReferenceType referenceType : conceptType.getProvidesConcept()) {
                String providingConceptId = conceptType.getRefId();
                providedConcepts.computeIfAbsent(referenceType.getRefId(), providedConceptId -> new LinkedHashSet<>())
                    .add(Concept.ProvidedConcept.builder()
                        .providingConceptId(providingConceptId)
                        .providedConceptId(referenceType.getRefId())
                        .activation(getActivation(referenceType))
                        .build());
            }
        }
        return providedConcepts;
    }

    private static Concept.Activation getActivation(ProvidesReferenceType referenceType) {
        return Concept.Activation.valueOf(referenceType.getActivation()
            .name());
    }

    private Map<String, Severity> getIncludedReferences(List<? extends IncludedReferenceType> referenceType) throws RuleException {
        Map<String, Severity> references = new HashMap<>();
        for (IncludedReferenceType includedReferenceType : referenceType) {
            Severity severity = getSeverity(includedReferenceType.getSeverity(), this::getDefaultIncludeSeverity);
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
     *     The severity type.
     * @return The severity.
     */
    private Severity getSeverity(SeverityEnumType severityType, Supplier<Severity> defaultSeveritySupplier) throws RuleException {
        String value = severityType != null ? severityType.value() : null;
        return getSeverity(value, defaultSeveritySupplier);
    }
}
