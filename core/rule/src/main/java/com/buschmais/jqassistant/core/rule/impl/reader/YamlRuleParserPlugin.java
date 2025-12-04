package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.model.Concept.Activation;
import com.buschmais.jqassistant.core.rule.api.model.Parameter.Type;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;

import static com.buschmais.jqassistant.core.rule.api.model.Concept.Activation.IF_AVAILABLE;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

public class YamlRuleParserPlugin extends AbstractRuleParserPlugin {

    private JsonSchemaValidator validator;

    private static final String YAML_EXTENSION_LONG = ".yaml";
    private static final String YAML_EXTENSION_SHORT = ".yml";

    @Override
    public void initialize() throws RuleException {
        validator = new JsonSchemaValidator();
    }

    @Override
    public boolean accepts(RuleSource ruleSource) throws RuleException {
        try {
            String fileName = ruleSource.getURL()
                .toExternalForm()
                .toLowerCase();
            return fileName.endsWith(YAML_EXTENSION_LONG) || fileName.endsWith(YAML_EXTENSION_SHORT);
        } catch (IOException e) {
            throw new RuleException("Unable to get the URL of the rule source.", e);
        }
    }

    protected void doParse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException {
        RuleContext context = new RuleContext(ruleSource, ruleSetBuilder);

        try {
            ValidationResult validationResult = validator.validate(ruleSource);
            if (!validationResult.isSourceWasEmpty() && validationResult.hasErrors()) {
                throw new RuleException(ruleSource + " has validation errors: " + validationResult.getValidationMessages()
                    .stream()
                    .map(Object::toString)
                    .collect(joining("; ")));
            }

            try (InputStream inputStream = ruleSource.getInputStream(); Reader reader = new YamlUnicodeReader(inputStream)) {
                LoadSettings settings = LoadSettings.builder()
                    .build();
                Load load = new Load(settings);
                Iterable<Object> objects = load.loadAllFromReader(reader);

                for (Object object : objects) {
                    // if read document is empty, object might be null
                    if (null == object) {
                        continue;
                    }

                    if (Map.class.isAssignableFrom(object.getClass())) {
                        processDocument((Map<String, Object>) object, context);
                    } else {
                        throw new RuleException("Cannot process rules from '" + ruleSource.getId() + "'.");
                    }
                }
            }
        } catch (IOException e) {
            String message = format("Cannot read rules from '%s'.", ruleSource.getId());
            throw new RuleException(message, e);
        } catch (ClassCastException e) {
            String message = format("Cannot process rules from '%s' because of an invalid YAML datastructure", ruleSource.getId());
            throw new RuleException(message);
        }
    }

    private void processDocument(Map<String, Object> documentMap, RuleContext context) throws RuleException {
        /*
         * There is no check for missing keys on the top level of an document as an
         * empty YAML document is a valid rule document.
         */

        boolean containsConcepts = documentMap.containsKey(CONCEPTS);
        boolean containsConstraints = documentMap.containsKey(CONSTRAINTS);
        boolean containsGroups = documentMap.containsKey(GROUPS);

        if (containsConcepts) {
            List<Map<String, Object>> executableRules = (List<Map<String, Object>>) ofNullable(documentMap.get(CONCEPTS)).orElse(emptyList());

            for (Map<String, Object> executableRule : executableRules) {
                Concept.ConceptBuilder builder = Concept.builder();
                String conceptId = this.processExecutableRule(executableRule, context, builder, this::getDefaultConceptSeverity);
                Set<Concept.ProvidedConcept> providedConcepts = this.extractProvidedConcepts(conceptId, executableRule);
                builder.providedConcepts(providedConcepts);
                context.getBuilder()
                    .addConcept(builder.build());
            }
        }

        if (containsConstraints) {
            List<Map<String, Object>> executableRules = (List<Map<String, Object>>) ofNullable(documentMap.get(CONSTRAINTS)).orElse(emptyList());

            for (Map<String, Object> executableRule : executableRules) {
                Constraint.ConstraintBuilder builder = Constraint.builder();
                this.processExecutableRule(executableRule, context, builder, this::getDefaultConstraintSeverity);
                context.getBuilder()
                    .addConstraint(builder.build());
            }
        }

        if (containsGroups) {
            List<Map<String, Object>> groupRules = (List<Map<String, Object>>) ofNullable(documentMap.get(GROUPS)).orElse(emptyList());

            for (Map<String, Object> groupRule : groupRules) {
                processGroup(groupRule, context);
            }
        }
    }

    private void processGroup(Map<String, Object> map, RuleContext context) throws RuleException {

        String id = (String) map.get(ID);
        List<Map<String, Object>> concepts = (List<Map<String, Object>>) map.computeIfAbsent(INCLUDED_CONCEPTS, key -> emptyList());
        Map<String, Set<Concept.ProvidedConcept>> providedConcepts = new HashMap<>();
        List<Map<String, Object>> constraints = (List<Map<String, Object>>) map.computeIfAbsent(INCLUDED_CONSTRAINTS, key -> emptyList());

        List<Map<String, Object>> groups = (List<Map<String, Object>>) map.computeIfAbsent(INCLUDED_GROUPS, key -> emptyList());

        SeverityMap includedGroups = new SeverityMap();
        SeverityMap includedConstraints = new SeverityMap();
        SeverityMap includedConcepts = new SeverityMap();

        for (Map<String, Object> refSpec : concepts) {
            RuleSeverityAssociation reference = extractRuleReferencesFrom(refSpec);
            includedConcepts.add(reference);
            for (Concept.ProvidedConcept providedConcept : extractProvidedConcepts(reference.getRuleName(), refSpec)) {
                providedConcepts.computeIfAbsent(providedConcept.getProvidedConceptId(), key -> new LinkedHashSet<>())
                    .add(providedConcept);
            }
        }

        for (Map<String, Object> refSpec : constraints) {
            RuleSeverityAssociation references = extractRuleReferencesFrom(refSpec);
            includedConstraints.add(references);
        }

        for (Map<String, Object> refSpec : groups) {
            RuleSeverityAssociation reference = extractRuleReferencesFrom(refSpec);
            includedGroups.add(reference);
        }

        String severityVal = (String) map.get(SEVERITY);
        Severity severity = getSeverity(severityVal, this::getDefaultGroupSeverity);
        String description = IndentHelper.removeIndent((String) map.get(DESCRIPTION));

        Group group = Group.builder()
            .id(id)
            .description(description)
            .severity(severity)
            .ruleSource(context.getSource())
            .concepts(includedConcepts)
            .constraints(includedConstraints)
            .providedConcepts(providedConcepts)
            .groups(includedGroups)
            .build();

        context.getBuilder()
            .addGroup(group);
    }

    private RuleSeverityAssociation extractRuleReferencesFrom(Map<String, Object> refSpec) throws RuleException {
        String refId = (String) refSpec.get(REF_ID);
        String severityVal = (String) refSpec.get(SEVERITY);
        Severity severity = getSeverity(severityVal, this::getDefaultIncludeSeverity);

        return new RuleSeverityAssociation(refId, severity);
    }

    private <T extends AbstractExecutableRule, B extends AbstractExecutableRule.Builder<B, T>> String processExecutableRule(Map<String, Object> map,
        RuleContext context, B builder, Supplier<Severity> defaultSeveritySupplier) throws RuleException {

        String id = (String) map.get(ID);
        String description = IndentHelper.removeIndent((String) map.get(DESCRIPTION));
        String source = (String) map.get(SOURCE);
        String language = (String) map.get(LANGUAGE);

        Executable<?> executable;

        if (CYPHER.equals(language) || null == language) {
            executable = new CypherExecutable(source);
        } else {
            executable = new ScriptExecutable(language, source);
        }

        Map<String, Boolean> required = extractRequiredConcepts(map);
        Map<String, Parameter> parameters = extractParameters(map);
        Verification verification = extractVerification(map);
        Report report = extractReportConfiguration(map);
        Severity severity = getSeverity((String) map.get(SEVERITY), defaultSeveritySupplier);

        builder.id(id)
            .description(description)
            .severity(severity)
            .executable(executable)
            .requiresConcepts(required)
            .parameters(parameters)
            .verification(verification)
            .report(report)
            .ruleSource(context.getSource());
        return id;
    }

    protected Report extractReportConfiguration(Map<String, Object> map) {
        Report.ReportBuilder reportBuilder = Report.builder();

        Optional<Map<String, Object>> reportBlockOpt = ofNullable((Map<String, Object>) map.get(REPORT));

        if (reportBlockOpt.isPresent()) {
            Map<String, Object> reportBlock = reportBlockOpt.get();

            if (reportBlock.containsKey(REPORT_TYPE)) {
                String reportType = (String) reportBlock.get(REPORT_TYPE);
                reportBuilder.selectedTypes(Report.selectTypes(reportType));
            }

            if (reportBlock.containsKey(PRIMARY_COLUMN)) {
                String primaryColumn = (String) reportBlock.get(PRIMARY_COLUMN);
                reportBuilder.primaryColumn(primaryColumn);
            }

            if (reportBlock.containsKey(KEY_COLUMNS)) {
                List<String> keyColumnsList = new ArrayList<>();
                keyColumnsList = Arrays.asList(((String) reportBlock.get(KEY_COLUMNS)).split("\\s*,\\s*"));
                reportBuilder.keyColumns(keyColumnsList);
            }

            if (reportBlock.containsKey(REPORT_PROPERTIES)) {
                Map<String, String> propertiesMap = (Map<String, String>) reportBlock.get(REPORT_PROPERTIES);

                Properties reportProperties = new Properties();

                Consumer<String> propertyConsumer = key -> {
                    Object val = propertiesMap.get(key);
                    reportProperties.put(key, val);
                };

                propertiesMap.keySet()
                        .forEach(propertyConsumer);

                reportBuilder.properties(reportProperties);
            }
        }

        return reportBuilder.build();
    }

    private Verification extractVerification(Map<String, Object> map) {
        /*
         * NOTES ON THE IMPLEMENTATION
         *
         * - It is totally fine if there is only the verify keyword given but no
         *   specific verification method. We do not want to restrict the user to much.
         */
        Verification verification = null;

        if (map.containsKey(VERIFY)) {
            Map<String, Map<String, Object>> verify = (Map<String, Map<String, Object>>) map.get(VERIFY);

            boolean hasAggregation = verify.containsKey(AGGREGATION);

            if (hasAggregation) {
                Map<String, Object> config = verify.get(AGGREGATION);

                String columnName = (String) config.get(AGGREGATION_COLUMN);
                Integer min = (Integer) config.get(AGGREGATION_MIN);
                Integer max = (Integer) config.get(AGGREGATION_MAX);

                verification = AggregationVerification.builder()
                    .column(columnName)
                    .min(min)
                    .max(max)
                    .build();
            } else {
                Map<String, Object> config = verify.get(ROW_COUNT);

                Integer min = (Integer) config.get(ROW_COUNT_MIN);
                Integer max = (Integer) config.get(ROW_COUNT_MAX);

                verification = RowCountVerification.builder()
                    .min(min)
                    .max(max)
                    .build();
            }
        }

        return verification;
    }

    private Map<String, Boolean> extractRequiredConcepts(Map<String, Object> map) {
        Map<String, Boolean> requiredConcepts = new HashMap<>();

        boolean hasRequiresSection = map.containsKey(REQUIRES_CONCEPTS);

        if (hasRequiresSection) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) map.get(REQUIRES_CONCEPTS);

            for (Map<String, Object> required : list) {
                String refIdVal = (String) required.get(REF_ID);
                Boolean optionalVal = (Boolean) required.get(OPTIONAL);

                Boolean aBoolean = ofNullable(optionalVal).orElse(Boolean.FALSE);

                requiredConcepts.put(refIdVal, aBoolean);
            }
        }

        return unmodifiableMap(requiredConcepts);
    }

    private Set<Concept.ProvidedConcept> extractProvidedConcepts(String providingConceptId, Map<String, Object> map) {
        Set<Concept.ProvidedConcept> providedConcepts = new LinkedHashSet<>();

        boolean hasProvidesSection = map.containsKey(PROVIDES_CONCEPTS);

        if (hasProvidesSection) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) map.get(PROVIDES_CONCEPTS);
            for (Map<String, Object> required : list) {
                String refIdVal = (String) required.get(REF_ID);
                Activation activationVal = Activation.valueOf(
                    LOWER_HYPHEN.to(UPPER_UNDERSCORE, (String) required.getOrDefault(ACTIVATION, UPPER_UNDERSCORE.to(LOWER_HYPHEN, IF_AVAILABLE.name()))));
                providedConcepts.add(Concept.ProvidedConcept.builder()
                    .providingConceptId(providingConceptId)
                    .providedConceptId(refIdVal)
                    .activation(activationVal)
                    .build());
            }

        }

        return unmodifiableSet(providedConcepts);
    }

    private Map<String, Parameter> extractParameters(Map<String, Object> map) {
        Map<String, Parameter> parameters = emptyMap();

        boolean hasParameters = map.containsKey(REQUIRES_PARAMETERS);

        if (hasParameters) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) map.computeIfAbsent(REQUIRES_PARAMETERS, key -> emptyList());
            parameters = new HashMap<>();

            for (Map<String, Object> parameterSpec : list) {
                String nameVal = (String) parameterSpec.get(PARAMETER_NAME);
                String defaultVal = (String) parameterSpec.get(PARAMETER_DEFAULT_VALUE);
                String typeVal = (String) parameterSpec.get(PARAMETER_TYPE);

                Parameter.Type type = toType(typeVal);
                Parameter parameter = new Parameter(nameVal, type, defaultVal);
                parameters.put(nameVal, parameter);
            }
        }

        return parameters;
    }

    private static Type toType(String value) {
        return Type.valueOf(value.toUpperCase());
    }

    @Getter
    @RequiredArgsConstructor
    private static class RuleContext {
        private final RuleSource source;
        private final RuleSetBuilder builder;
    }

    static class SeverityMap extends HashMap<String, Severity> {
        public void add(RuleSeverityAssociation reference) {
            put(reference.getRuleName(), reference.getSeverity());
        }
    }

    @Getter
    @RequiredArgsConstructor
    static class RuleSeverityAssociation {
        private final String ruleName;
        private final Severity severity;
    }
}
