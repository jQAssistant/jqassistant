package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;

import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.model.Parameter.Type;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;


/* todo check if the naming is consistent (*Block for YAML structures)
 *
 */
public class YamlRuleParserPlugin extends AbstractRuleParserPlugin {
    private static JsonSchemaValidator validator;

    private static final String YAML_EXTENSION_LONG = ".yaml";
    private static final String YAML_EXTENSION_SHORT = ".yml";

    private ErrorMessageGenerator errorMessageGenerator;


    @Override
    public void initialize() throws RuleException {
        validator = new JsonSchemaValidator();
        errorMessageGenerator = new ErrorMessageGenerator();
    }

    @Override
    public boolean accepts(RuleSource ruleSource) throws RuleException {
        try {
            boolean acceptable = ruleSource.getURL().toExternalForm().toLowerCase().endsWith(YAML_EXTENSION_LONG) ||
                                 ruleSource.getURL().toExternalForm().toLowerCase().endsWith(YAML_EXTENSION_SHORT);
            return acceptable;
        } catch (IOException e) {
            String message = "Unable to get the URL of the rule source.";
            throw new RuleException(message, e);
        }
    }

    protected void doParse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException {
        RuleContext context = new RuleContext(ruleSource, ruleSetBuilder);

        try {
            ValidationResult validationResult = validator.validate(ruleSource);

            boolean throwException = !validationResult.isSourceWasEmpty() &&
                                     validationResult.hasErrors();

            if (throwException) {
                String message = errorMessageGenerator.generate(ruleSource, validationResult.getValidationMessages());
                throw new RuleException(message);
            }

            try (InputStream inputStream = ruleSource.getInputStream();
                 Reader reader = new YamlUnicodeReader(inputStream)) {
                LoadSettings settings = LoadSettings.builder().build();
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
            String message = format("Cannot process rules from '%s' because of an invalid YAML datastructure",
                                    ruleSource.getId());
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
            RuleConsumer<Concept> conceptRuleConsumer = concept -> context.getBuilder().addConcept(concept);
            List<Map<String, Object>> executableRules =
                (List<Map<String, Object>>) ofNullable(documentMap.get(CONCEPTS)).orElse(emptyList());

            for (Map<String, Object> executableRule : executableRules) {
                this.processExecutableRule(executableRule, context, conceptRuleConsumer, Concept.builder());
            }
        }

        if (containsConstraints) {
            RuleConsumer<Constraint> ruleConsumer = constraint -> context.getBuilder().addConstraint(constraint);
            List<Map<String, Object>> executableRules =
                (List<Map<String, Object>>) ofNullable(documentMap.get(CONSTRAINTS)).orElse(emptyList());

            for (Map<String, Object> executableRule : executableRules) {
                this.processExecutableRule(executableRule, context, ruleConsumer, Constraint.builder());
            }
        }

        if (containsGroups) {
            List<Map<String, Object>> groupRules =
                (List<Map<String, Object>>) ofNullable(documentMap.get(GROUPS)).orElse(emptyList());

            for (Map<String, Object> groupRule : groupRules) {
                processGroup(groupRule, context);
            }
        }
    }

    private void processGroup(Map<String, Object> map, RuleContext context)
        throws RuleException {

        String id = (String)map.get(ID);
        // todo computeIfAbsend ersetzen
        List<Map<String, String>> concepts = (List<Map<String, String>>) map.computeIfAbsent(INCLUDED_CONCEPTS,
                                                                                             key -> emptyList());
        // todo computeIfAbsend ersetzen
        List<Map<String, String>> constraints = (List<Map<String, String>>) map.computeIfAbsent(INCLUDED_CONSTRAINTS,
                                                                                                key -> emptyList());
        // todo computeIfAbsend ersetzen
        //List<Map<String, String>> groups = getMapByKey(map, INCLUDES_GROUPS);


        List<Map<String, String>> groups = (List<Map<String, String>>) map.computeIfAbsent(INCLUDED_GROUPS,
                                                                                           key -> emptyList());


        SeverityMap includedGroups = new SeverityMap();
        SeverityMap includedConstraints = new SeverityMap();
        SeverityMap includedConcepts = new SeverityMap();

        for (Map<String, String> refSpec : concepts) {
            RuleSeverityAssociation reference = extractRuleReferencesFrom(refSpec);
            includedConcepts.add(reference);
        }

        for (Map<String, String> refSpec : constraints) {
            RuleSeverityAssociation references = extractRuleReferencesFrom(refSpec);
            includedConstraints.add(references);
        }

        for (Map<String, String> refSpec : groups) {
            RuleSeverityAssociation reference = extractRuleReferencesFrom(refSpec);
            includedGroups.add(reference);
        }

        String severityVal = (String) map.get(SEVERITY);
        Severity severity = severityVal == null ? null : toSeverity(severityVal);

        Group group = Group.builder().id(id).severity(severity)
                           .ruleSource(context.getSource())
                           .concepts(includedConcepts).constraints(includedConstraints)
                           .groups(includedGroups)
                           .build();

        context.getBuilder().addGroup(group);
    }

    private RuleSeverityAssociation extractRuleReferencesFrom(Map<String, String> refSpec) throws RuleException {
        String refId = refSpec.get(REF_ID);
        String severityVal = refSpec.get(SEVERITY);
        Severity severity = severityVal == null ? null : toSeverity(severityVal);

        return new RuleSeverityAssociation(refId, severity);
    }

    private <T extends AbstractExecutableRule, B extends AbstractExecutableRule.Builder<B, T>> void processExecutableRule(Map<String, Object> map,
                                                                                                                          RuleContext context,
                                                                                                                          RuleConsumer<T> consumer,
                                                                                                                          B builder)
        throws RuleException {

        String id = (String) map.get(ID);
        String description = (String) map.get(DESCRIPTION);
        String source = (String) map.get(SOURCE);
        String language = (String) map.get(LANGUAGE);
        String serverityV = (String) map.get(SEVERITY);

        Executable<?> executable = null;

        if (CYPHER.equals(language) || null == language) {
            executable = new CypherExecutable(source);
        } else {
            executable = new ScriptExecutable(language, source);
        }

        Map<String, Boolean> required = extractRequiredConcepts(map);
        Map<String, Parameter> parameters = extractParameters(map);
        Verification verification = extractVerification(map);
        Report report = extractReportConfiguration(map);
        Severity severity = toSeverity(serverityV);

        T rule = builder.id(id)
                        .description(description)
                        .severity(severity)
                        .executable(executable)
                        .requiresConcepts(required)
                        .parameters(parameters)
                        .verification(verification)
                        .report(report)
                        .ruleSource(context.getSource()).build();

        consumer.consume(rule);
    }

    protected Report extractReportConfiguration(Map<String, Object> map) {
        Report.ReportBuilder reportBuilder = Report.builder();

        Optional<Map<String, Object>> reportBlockOpt = ofNullable((Map<String, Object>)map.get(REPORT));

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

            if (reportBlock.containsKey(REPORT_PROPERTIES)) {
                Map<String, String> propertiesMap = (Map<String, String>) reportBlock.get(REPORT_PROPERTIES);

                Properties reportProperties = new Properties();

                Consumer<String> propertyConsumer = key -> {
                    Object val = propertiesMap.get(key);
                    reportProperties.put(key, val);
                };

                propertiesMap.keySet().forEach(propertyConsumer);

                reportBuilder.properties(reportProperties);
            }
        }

        Report report = reportBuilder.build();

        return report;
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
            List<Map<String, Object>> list = (List<Map<String, Object>>)map.get(REQUIRES_CONCEPTS);

            for (Map<String, Object> required : list) {
                String refIdVal = (String) required.get(REF_ID);
                Boolean optionalVal = (Boolean) required.get(OPTIONAL);

                Boolean aBoolean = ofNullable(optionalVal).orElse(Boolean.FALSE);

                requiredConcepts.put(refIdVal, aBoolean);
            }
        }

        return Collections.unmodifiableMap(requiredConcepts);
    }

    private Map<String, Parameter> extractParameters(Map<String, Object> map) {
        Map<String, Parameter> parameters = emptyMap();

        boolean hasParameters = map.containsKey(REQUIRES_PARAMETERS);

        if (hasParameters) {
            // todo computeIfAbsend ersetzen
            List<Map<String, Object>> list = (List<Map<String, Object>>) map.computeIfAbsent(REQUIRES_PARAMETERS,
                                                                                             key -> emptyList());
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

    private Severity toSeverity(String value) throws RuleException {
        if (value == null) {
            return getRuleConfiguration().getDefaultConceptSeverity();
        }

        Severity severity = Severity.fromValue(value.toLowerCase());
        return severity != null ? severity : getRuleConfiguration().getDefaultConceptSeverity();
    }

    private static class RuleContext {
        private RuleSource source;
        private RuleSetBuilder builder;

        public RuleContext(RuleSource source, RuleSetBuilder builder) {
            this.source = source;
            this.builder = builder;
        }

        public RuleSource getSource() {
            return source;
        }

        public RuleSetBuilder getBuilder() {
            return builder;
        }
    }

    private interface RuleConsumer<T> {
        void consume(T t) throws RuleException;
    }

    static class SeverityMap extends HashMap<String, Severity> {
        public void add(RuleSeverityAssociation reference) {
            put(reference.getRuleName(), reference.getSeverity());
        }
    }

    static class RuleSeverityAssociation {
        private String ruleName;
        private Severity severity;

        public RuleSeverityAssociation(String ruleName, Severity severity) {
            this.ruleName = ruleName;
            this.severity = severity;
        }

        public String getRuleName() {
            return ruleName;
        }

        public Severity getSeverity() {
            return severity;
        }

    }
}
