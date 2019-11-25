package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.rule.api.model.AbstractExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.CypherExecutable;
import com.buschmais.jqassistant.core.rule.api.model.Group;
import com.buschmais.jqassistant.core.rule.api.model.Parameter;
import com.buschmais.jqassistant.core.rule.api.model.Parameter.Type;
import com.buschmais.jqassistant.core.rule.api.model.Report;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.model.Verification;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;


/* todo check if the naming is consistent (*Block for YAML structures)
 *
 */
public class YamlRuleParserPlugin extends AbstractRuleParserPlugin {
    private static final String YAML_EXTENSION_LONG = ".yaml";
    private static final String YAML_EXTENSION_SHORT = ".yml";

    private static final KeySet TOPLEVEL_KEYS = new KeySet(CONCEPTS, CONSTRAINTS, GROUPS);

    private static final KeySet GROUP_KEYS = new KeySet(ID, INCLUDES_CONCEPTS, INCLUDES_CONSTRAINTS,
                                                        INCLUDES_GROUPS, SEVERITY);


    private static final KeySet GROUP_KEYS_REQUIRED = new KeySet(ID);

    private static final KeySet CONCEPT_KEYS = new KeySet(AGGREGATION, DESCRIPTION, ID, CYPHER,
                                                          REQUIRES_CONCEPTS, REPORT, REQUIRES_PARAMETERS,
                                                          SEVERITY, SOURCE, VERIFY);

    private static final KeySet CONCEPT_KEYS_REQUIRED = new KeySet(ID, CYPHER);

    private static final KeySet PARAMETER_KEYS = new KeySet(PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_DEFAULT_VALUE);

    private static final KeySet PARAMETER_KEYS_REQUIRED = new KeySet(PARAMETER_NAME, PARAMETER_TYPE);

    private static final KeySet REPORT_KEYS = new KeySet(PRIMARY_COLUMN, REPORT_TYPE, REPORT_PROPERTIES);

    private static final KeySet REPORT_KEYS_REQUIRED = new KeySet();

    private static final KeySet RULE_REFERENCE_KEYS = new KeySet(REF_ID, SEVERITY);

    private static final KeySet RULE_REFERENCE_KEYS_REQUIRED = new KeySet(REF_ID);

    private KeySet REQUIRES_CONCEPTS_KEYS_REQUIRED = new KeySet(REF_ID);
    private KeySet REQUIRES_CONCEPTS_KEYS = new KeySet(OPTIONAL, REF_ID);

    private static final KeySet VERIFY_KEYS = new KeySet(AGGREGATION, ROW_COUNT);
    private static final KeySet VERIFY_KEYS_REQUIRED = new KeySet();


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
                    processDocument((Map<String, Object>)object, context);
                } else {
                    throw new RuleException("Cannot process rules from '" + ruleSource.getId() + "'.");
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
        KeySet foundKeys = new KeySet(documentMap.keySet());
        boolean hasUnsupportedKeys = !TOPLEVEL_KEYS.containsKeyset(foundKeys);

        if (hasUnsupportedKeys) {
            KeySet unsupportedKeys = TOPLEVEL_KEYS.getDifference(foundKeys);
            String message = format("Rule source '%s' contains the following unsupported keys: %s",
                                    context.getSource().getId(), String.join(", ", unsupportedKeys));
            throw new RuleException(message);
        }

        /*
         * There is no check for missing keys on the top level of an document as an
         * empty YAML document is a valid rule document.
         */

        boolean containsConcepts = documentMap.containsKey(CONCEPTS);
        boolean containsConstraints = documentMap.containsKey(CONSTRAINTS);
        boolean containsGroups = documentMap.containsKey(GROUPS);

        if (containsConcepts) {
            context.setRuleType(CONCEPT);

            RuleConsumer<Concept> conceptRuleConsumer = concept -> context.getBuilder().addConcept(concept);
            List<Map<String, Object>> executableRules =
                (List<Map<String, Object>>) ofNullable(documentMap.get(CONCEPTS)).orElse(emptyList());

            for (Map<String, Object> executableRule : executableRules) {
                this.processExecutableRule(executableRule, context, conceptRuleConsumer, Concept.builder());
            }
        }

        if (containsConstraints) {
            context.setRuleType(CONSTRAINT);
            RuleConsumer<Constraint> ruleConsumer = constraint -> context.getBuilder().addConstraint(constraint);
            List<Map<String, Object>> executableRules =
                (List<Map<String, Object>>) ofNullable(documentMap.get(CONSTRAINTS)).orElse(emptyList());

            for (Map<String, Object> executableRule : executableRules) {
                this.processExecutableRule(executableRule, context, ruleConsumer, Constraint.builder());
            }
        }

        if (containsGroups) {
            context.setRuleType(GROUP);

            List<Map<String, Object>> groupRules =
                (List<Map<String, Object>>) ofNullable(documentMap.get(GROUPS)).orElse(emptyList());

            for (Map<String, Object> groupRule : groupRules) {
                processGroup(groupRule, context);
            }
        }

        context.clearRuleType();
    }

    private void processGroup(Map<String, Object> map, RuleContext context)
        throws RuleException {
        KeySet foundKeys = new KeySet(map.keySet());
        boolean hasRequiredKey = foundKeys.containsKeyset(GROUP_KEYS_REQUIRED);
        boolean hasUnsupportedKeys = !GROUP_KEYS.containsKeyset(foundKeys);

        if (!hasRequiredKey) {
            KeySet missingKeys = foundKeys.getDifference(GROUP_KEYS_REQUIRED);

            String message = format("Rule source with id '%s' contains a group with the following missing keys: %s",
                                    context.getSource().getId(), join(", ", missingKeys));
            throw new RuleException(message);
        }

        if (hasUnsupportedKeys) {
            KeySet unsupportedKeys = GROUP_KEYS.getDifference(foundKeys);

            String message = format("Rule source with id '%s' contains a group containing the following " +
                                    "unsupported keys: %s",
                                    context.getSource().getId(), join(", ", unsupportedKeys));
            throw new RuleException(message);
        }

        String id = (String)map.get(ID);
        // todo computeIfAbsend ersetzen
        List<Map<String, String>> concepts = (List<Map<String, String>>) map.computeIfAbsent(INCLUDES_CONCEPTS,
                                                                                             key -> emptyList());
        // todo computeIfAbsend ersetzen
        List<Map<String, String>> constraints = (List<Map<String, String>>) map.computeIfAbsent(INCLUDES_CONSTRAINTS,
                                                                                                key -> emptyList());
        // todo computeIfAbsend ersetzen
        //List<Map<String, String>> groups = getMapByKey(map, INCLUDES_GROUPS);


        List<Map<String, String>> groups = (List<Map<String, String>>) map.computeIfAbsent(INCLUDES_GROUPS,
                                                                                           key -> emptyList());


        SeverityMap includedGroups = new SeverityMap();
        SeverityMap includedConstraints = new SeverityMap();
        SeverityMap includedConcepts = new SeverityMap();

        for (Map<String, String> refSpec : concepts) {
            RuleSeverityAssociation reference = extractRuleReferencesFrom(id, refSpec, CONCEPT, context);
            includedConcepts.add(reference);
        }

        for (Map<String, String> refSpec : constraints) {
            RuleSeverityAssociation references = extractRuleReferencesFrom(id, refSpec, CONSTRAINT, context);
            includedConstraints.add(references);
        }

        for (Map<String, String> refSpec : groups) {
            RuleSeverityAssociation reference = extractRuleReferencesFrom(id, refSpec, GROUP, context);
            includedGroups.add(reference);
        }

        Group group = Group.builder().id(id).severity(null /* todo Was ist hier der Defaultwert?*/)
                           .ruleSource(context.getSource())
                           .concepts(includedConcepts).constraints(includedConstraints)
                           .groups(includedGroups)
                           .build();

        context.getBuilder().addGroup(group);
    }

    private RuleSeverityAssociation extractRuleReferencesFrom(String containingRuleId, Map<String, String> refSpec,
                                                                  String role, RuleContext context)
        throws RuleException {
        KeySet foundKeys = new KeySet(refSpec.keySet());
        boolean hasRequiredKeys = foundKeys.containsKeyset(RULE_REFERENCE_KEYS_REQUIRED);
        boolean hasUnsupportedKeys = !RULE_REFERENCE_KEYS.containsKeyset(foundKeys);

        if (!hasRequiredKeys) {
            KeySet missingKeys = foundKeys.getDifference(RULE_REFERENCE_KEYS_REQUIRED);

            String message = format("Rule source '%s' contains the group '%s' with an included %s without " +
                                    "the following required keys: %s",
                                    context.getSource().getId(), containingRuleId, role, join(", ", missingKeys));
            throw new RuleException(message);
        }

        if (hasUnsupportedKeys) {
            KeySet unsupportedKeys = RULE_REFERENCE_KEYS.getDifference(foundKeys);

            String message = format("Rule source '%s' contains the group '%s' with an included %s with the " +
                                    "following unsupported keys: %s",
                                    context.getSource().getId(), containingRuleId, role, join(", ", unsupportedKeys));
            throw new RuleException(message);
        }

        String refId = refSpec.get(REF_ID);
        String severityVal = refSpec.get(SEVERITY);

        if (refSpec.containsKey(SEVERITY) && severityVal == null) {
            String message = format("Rule source '%s' contains the group '%s' with an included %s without " +
                                    "specified value for its severity",
                                    context.getSource().getId(), containingRuleId, role);
            throw new RuleException(message);
        }

        // todo Wie ist hier das erwartete Verhalten?
        // Was ist, wenn keine Severity angegeben wurde
        Severity severity = severityVal == null ? null
                                                : toSeverity(severityVal, context);


        return new RuleSeverityAssociation(refId, severity);
    }

    private <T extends AbstractExecutableRule, B extends AbstractExecutableRule.Builder<B, T>> void processExecutableRule(Map<String, Object> map,
                                                                                                                          RuleContext context,
                                                                                                                          RuleConsumer<T> consumer,
                                                                                                                          B builder)
        throws RuleException {
        KeySet foundKeys = new KeySet(map.keySet());
        boolean hasRequiredKeys = foundKeys.containsKeyset(CONCEPT_KEYS_REQUIRED);
        boolean hasUnsupportedKeys = !CONCEPT_KEYS.containsKeyset(foundKeys);

        if (!hasRequiredKeys) {
            KeySet missingKeys = foundKeys.getDifference(CONCEPT_KEYS_REQUIRED);

            String message = format("A %s in rule source '%s' has missing required keys. The following keys " +
                                    "are missing: %s",
                                    context.getRuleType(), context.getSource().getId(), join(", ", missingKeys));
            throw new RuleException(message);
        } else if (hasUnsupportedKeys) {
            String message = format("Rule source '%s' contains a concept with one or more unknown keys: %s",
                                    context.getSource().getId(), join(", ", foundKeys));
            throw new RuleException(message);
        }

        String id = (String) map.get(ID);
        String description = (String) map.get(DESCRIPTION);
        String cypher = (String) map.get(CYPHER);
        String serverityV = (String) map.get(SEVERITY);
        String language = CYPHER; // todo? So richtig?

        CypherExecutable executable = new CypherExecutable(cypher);
        Map<String, Boolean> required = extractRequiredConcepts(map, id, context);
        Map<String, Parameter> parameters = extractParameters(map, id, context);
        Verification verification = extractVerifycation(map, id, context);
        Report report = extractReportConfiguration(map, context);
        Severity severity = toSeverity(serverityV, context);

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

    protected Report extractReportConfiguration(Map<String, Object> map, RuleContext context) throws RuleException {
        /**
         *
         * Aus dem Asciidoctor code
         Object primaryReportColum = part.getAttributes().get(PRIMARY_REPORT_COLUM);
         Object reportType = part.getAttributes().get(REPORT_TYPE);
         Properties reportProperties = parseProperties(part, REPORT_PROPERTIES);
         Report.ReportBuilder reportBuilder = Report.builder();
         if (reportType != null) {
         reportBuilder.selectedTypes(Report.selectTypes(reportType.toString()));
         }
         if (primaryReportColum != null) {
         reportBuilder.primaryColumn(primaryReportColum.toString());
         }
         return reportBuilder.properties(reportProperties).build();

         **/

        // todo Add support for report section
        // Example: report.xml
        Report.ReportBuilder reportBuilder = Report.builder();

        // todo only known keys present?
        // todo all required keys present?
        Optional<Map<String, Object>> reportBlockOpt = ofNullable((Map<String, Object>)map.get(REPORT));

        if (reportBlockOpt.isPresent()) {
            Map<String, Object> reportBlock = reportBlockOpt.get();
            KeySet foundKeys = new KeySet(reportBlock.keySet());

            boolean hasUnsupportedKeys = !REPORT_KEYS.containsKeyset(foundKeys);
            boolean hasRequiredKeys = foundKeys.containsKeyset(REPORT_KEYS_REQUIRED);

            if (!hasRequiredKeys)  {
                /* todo Klären mit Dirk, ob es überhaupt Pflichtkeys für die Reportsection gibt.
                 */
                // KeySet missingKeys = foundKeys.getDifference(REPORT_KEYS_REQUIRED);
            } else if (hasUnsupportedKeys) {
                KeySet unsupportedKeys = REPORT_KEYS.getDifference(foundKeys);

                // todo: The given message is wrong if we process a constraint
                // todo: use better the phrase "with one or more unknown or misplaced keys"
                String message = format("Rule source '%s' contains a %s with " +
                                        "one or more unknown keys in the report block: %s",
                                        context.getSource().getId(), context.getRuleType(),
                                        join(", ", unsupportedKeys));
                throw new RuleException(message);
            }


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

    private Verification extractVerifycation(Map<String, Object> map, String ruleId, RuleContext context) throws RuleException {
        /*
         * NOTES ON THE IMPLEMENTATION
         *
         * - It is totally fine if there is only the verify keyword given but no
         *   specific verification method. We do not want to restrict the user to much.
         */
        Verification verification = null;

        /* todo Gibt welche Keys sind für eine Rowcount Verification notwendig? */
        /* todo Gibt welche Keys sind für eine Aggration Verification notwendig? */

        if (map.containsKey(VERIFY)) {
            Map<String, Map<String, Object>> verify = (Map<String, Map<String, Object>>) map.get(VERIFY);
            KeySet foundKeys = new KeySet(verify.keySet());

            boolean hasUnsupportedKeys = !VERIFY_KEYS.containsKeyset(foundKeys);

            if (hasUnsupportedKeys) {
                KeySet unsupportedKeys = VERIFY_KEYS.getDifference(foundKeys);
                String message = format("Rule '%s' in rule source with id '%s' contains unsupported keywords for " +
                                        "a verification. The following keys are not supported: %s",
                                        ruleId, context.getSource().getId(), String.join(", ", unsupportedKeys));
                throw new RuleException(message);
            }

            boolean hasAggregation = verify.containsKey(AGGREGATION);
            boolean hasRowCount = verify.containsKey(ROW_COUNT);

            if (hasAggregation && hasRowCount) {
                String message = format("Rule '%s' in rule source with id '%s' contains a %s with a verification " +
                                        "via row count and aggregation. Only one verification method can be used.",
                                        ruleId, context.getSource().getId(), context.getRuleType());

                throw new RuleException(message);
            } else if (hasAggregation) {
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

    private Map<String, Boolean> extractRequiredConcepts(Map<String, Object> map, String ruleId,
                                                         RuleContext context) throws RuleException {
        Map<String, Boolean> requiredConcepts = new HashMap<>();

        boolean hasRequiresSection = map.containsKey(REQUIRES_CONCEPTS);

        if (hasRequiresSection) {
            List<Map<String, Object>> list = (List<Map<String, Object>>)map.get(REQUIRES_CONCEPTS);

            for (Map<String, Object> required : list) {
                KeySet foundKeys = new KeySet(required.keySet());

                boolean hasRequiredKeys = foundKeys.containsKeyset(REQUIRES_CONCEPTS_KEYS_REQUIRED);
                boolean hasUnsupportedKeys = !REQUIRES_CONCEPTS_KEYS.containsKeyset(foundKeys);

                if (!hasRequiredKeys) {
                    KeySet missingKeys = foundKeys.getDifference(REQUIRES_CONCEPTS_KEYS_REQUIRED);

                    String message = format("The %s '%s' in rule source '%s' requires one or more concepts, " +
                                            "but a concept reference misses one or more required keys. " +
                                            "The following keys are missing: %s",
                                            context.getRuleType(), ruleId, context.getSource().getId(),
                                            join(", ", missingKeys));
                    throw new RuleException(message);
                } else if (hasUnsupportedKeys) {
                    KeySet unsupportedKeys = REQUIRES_CONCEPTS_KEYS.getDifference(foundKeys);
                    String message = format("The %s '%s' in rule source '%s' requires one or more concepts, " +
                                            "but a concept reference has one or more unsupported keys. " +
                                            "The following keys are unsupported: %s",
                                            context.getRuleType(), ruleId, context.getSource().getId(),
                                            join(", ", unsupportedKeys));
                    throw new RuleException(message);
                }

                String refIdVal = (String) required.get(REF_ID);
                Boolean optionalVal = (Boolean) required.get(OPTIONAL);

                Boolean aBoolean = ofNullable(optionalVal).orElse(Boolean.FALSE);

                requiredConcepts.put(refIdVal, aBoolean);
            }
        }

        return Collections.unmodifiableMap(requiredConcepts);
    }

    private Map<String, Parameter> extractParameters(Map<String, Object> map, String ruleId,
                                                     RuleContext context) throws RuleException {
        Map<String, Parameter> parameters = emptyMap();

        boolean hasParameters = map.containsKey(REQUIRES_PARAMETERS);

        if (hasParameters) {
            // todo computeIfAbsend ersetzen
            List<Map<String, Object>> list = (List<Map<String, Object>>) map.computeIfAbsent(REQUIRES_PARAMETERS,
                                                                                             key -> emptyList());
            parameters = new HashMap<>();

            for (Map<String, Object> parameterSpec : list) {
                KeySet foundKeys = new KeySet(parameterSpec.keySet());
                boolean hasRequiredKeys = foundKeys.containsKeyset(PARAMETER_KEYS_REQUIRED);
                boolean hasUnsupportedKeys = !PARAMETER_KEYS.containsKeyset(foundKeys);

                if (!hasRequiredKeys) {
                    KeySet missingKeys = foundKeys.getDifference(PARAMETER_KEYS_REQUIRED);

                    String message = format("The %s '%s' in rule source '%s' has an invalid parameter. " +
                                            "The following keys are missing: %s",
                                            context.getRuleType(), ruleId, context.getSource().getId(), join(", ", missingKeys));
                    throw new RuleException(message);
                } else if (hasUnsupportedKeys) {
                    KeySet unsupportedKeys = PARAMETER_KEYS.getDifference(foundKeys);
                    String message = format("The %s '%s' in rule source '%s' has an invalid parameter. The " +
                                            "following keys are not supported: %s",
                                            context.getRuleType(), ruleId, context.getSource().getId(),
                                            join(", ", unsupportedKeys));
                    throw new RuleException(message);
                }

                String nameVal = (String) parameterSpec.get(PARAMETER_NAME);
                String defaultVal = (String) parameterSpec.get(PARAMETER_DEFAULT_VALUE);
                String typeVal = (String) parameterSpec.get(PARAMETER_TYPE);

                if (nameVal == null || nameVal.isEmpty()) {
                    String message = format("A parameter of %s '%s' in rule source '%s' has no name",
                                            context.getRuleType(), ruleId, context.getSource().getId());
                    throw new RuleException(message);
                } else if (typeVal == null || typeVal.isEmpty()) {
                    String message = format("The parameter '%s' of %s '%s' in rule source '%s' has no " +
                                            "parameter type specified",
                                            nameVal, context.getRuleType(), ruleId, context.getSource().getId());
                    throw new RuleException(message);
                } else if (defaultVal == null && parameterSpec.containsKey(PARAMETER_DEFAULT_VALUE)) {
                    String message = format("The parameter '%s' of %s '%s' in rule source '%s' " +
                                                   "has no default value",
                                                   nameVal, context.getRuleType(), ruleId, context.getSource().getId());
                    throw new RuleException(message);
                }

                Parameter.Type type = toType(typeVal, context);
                Parameter parameter = new Parameter(nameVal, type, defaultVal);
                parameters.put(nameVal, parameter);
            }
        }

        return parameters;
    }

    private Type toType(String value, RuleContext context) throws RuleException {
        try {
            return Type.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            String message = format("'%s' is not a supported type for a parameter", value);
            throw new RuleException(message);
        }
    }

    private Severity toSeverity(String value, RuleContext context) throws RuleException {
        if (value == null) {
            return getRuleConfiguration().getDefaultConceptSeverity();
        }

        Severity severity = Severity.fromValue(value.toLowerCase());
        return severity != null ? severity : getRuleConfiguration().getDefaultConceptSeverity();
    }

    /**
     * Returns a map associated with a given key from another map.
     *
     * @    param parentMap The map which might contain a map for the given key
     * @    param key The key to lookup in the given map.
     *
     * @return The map associated with the key or a newly created empty map
     *
    private <T> T getMapByKey(Map<String, Object> parentMap, String key) {
        T result = (T) ofNullable(parentMap.get(key)).orElse(emptyMap());
        return result;
    }
    */

    private static class RuleContext {
        private RuleSource source;
        private RuleSetBuilder builder;
        private String ruleType;

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

        public String getRuleType() {
            return Optional.ofNullable(ruleType).orElseThrow(() -> new IllegalStateException("No rule type set!"));
        }

        public void setRuleType(String ruleType) {
            this.ruleType = ruleType;
        }

        public void clearRuleType() {
            ruleType = null;
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

    static class KeySet implements Iterable<String> {

        private final Set<String> keySet;

        /**
         * Creates an empty set of keys.
         */
        public KeySet() {
            keySet = emptySet();
        }

        KeySet(String... keys) {
            keySet = new TreeSet<>(asList(keys));
        }

        KeySet(Set<String> keys) {
            keySet = new TreeSet<>(keys);
        }

        boolean containsKeyset(KeySet other) {
            boolean containsAll = keySet.containsAll(other.keySet);
            return containsAll;
        }

        KeySet getDifference(KeySet other) {
            HashSet<String> workingCopy = new HashSet<>(other.keySet);

            workingCopy.removeAll(keySet);

            return new KeySet(workingCopy);
        }

        @Override
        public Iterator<String> iterator() {
            return keySet.iterator();
        }

        @Override
        public void forEach(Consumer<? super String> action) {
            keySet.forEach(action);
        }

        @Override
        public Spliterator<String> spliterator() {
            return keySet.spliterator();
        }

        public Stream<String> stream() {
            return keySet.stream();
        }

        @Override
        public String toString() {
            return keySet.toString();
        }
    }


}
