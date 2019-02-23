package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.CypherExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Parameter;
import com.buschmais.jqassistant.core.analysis.api.rule.Parameter.Type;
import com.buschmais.jqassistant.core.analysis.api.rule.Report;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleHandlingException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.api.rule.Verification;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.snakeyaml.engine.v1.api.Load;
import org.snakeyaml.engine.v1.api.LoadSettings;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v1.api.YamlUnicodeReader;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class YamlRuleParserPlugin extends AbstractRuleParserPlugin {
    private static final String YAML_EXTENSION_LONG = ".yaml";
    private static final String YAML_EXTENSION_SHORT = ".yml";

    private static final Collection<String> TOPLEVEL_KEYS =
        unmodifiableCollection(asList(CONCEPTS, CONSTRAINTS, GROUPS));

    private static final Collection<String> GROUP_KEYS =
        unmodifiableCollection(asList(ID, INCLUDES_CONCEPTS, INCLUDES_CONSTRAINTS,
                                      INCLUDES_GROUPS, SEVERITY));

    private static final Collection<String> GROUP_KEYS_REQUIRED = unmodifiableCollection(singletonList(ID));

    private static final Collection<String> CONCEPT_KEYS =
        unmodifiableCollection(asList(AGGREGATION, DESCRIPTION, ID, CYPHER, REQUIRES_CONCEPTS,
                                      REQUIRES_PARAMETERS, SEVERITY, SOURCE, VERIFY));

    private Collection<String> CONCEPT_KEYS_REQUIRED = unmodifiableCollection(asList(ID, CYPHER));

    private Collection<String> PARAMETER_KEYS =
        unmodifiableCollection(asList(PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_DEFAULT_VALUE));

    private Collection<String> PARAMETER_KEYS_REQUIRED = unmodifiableCollection(asList(PARAMETER_NAME, PARAMETER_TYPE));

    private Collection<String> RULE_REFERENCE_KEYS = unmodifiableCollection(asList(REF_ID, SEVERITY));

    private Collection<String> RULE_REFERENCE_KEYS_REQUIRED = unmodifiableCollection(singletonList(REF_ID));

    @Override
    public boolean accepts(RuleSource ruleSource) throws RuleException {
        try {
            boolean acceptable = ruleSource.getURL().toExternalForm().toLowerCase().endsWith(YAML_EXTENSION_LONG) ||
                                 ruleSource.getURL().toExternalForm().toLowerCase().endsWith(YAML_EXTENSION_SHORT);
            return acceptable;
        } catch (IOException e) {
            throw new RuleException("Unable to get the URL of the rule source.", e);
        }
    }

    protected void doParse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException {
        RuleContext context = new RuleContext(ruleSource, ruleSetBuilder);

        try (InputStream inputStream = ruleSource.getInputStream();
            Reader reader = new YamlUnicodeReader(inputStream)) {
            LoadSettings settings = new LoadSettingsBuilder().build();
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
            throw new RuleException("Cannot read rules from '" + ruleSource.getId() + "'.", e);
        } catch (ClassCastException e) {
            throw new RuleException("Cannot process rules from '" + ruleSource.getId() + "' " +
                                    "because of an invalid YAML datastructure");
        }
    }

    private void processDocument(Map<String, Object> documentMap, RuleContext context) throws RuleException {
        Set<String> givenKeys = documentMap.keySet();
        boolean onlyKnownKeys = TOPLEVEL_KEYS.containsAll(givenKeys);

        if (!onlyKnownKeys) {
            givenKeys.removeAll(TOPLEVEL_KEYS);

            throw new RuleException("Rule source '" + context.getSource().getId() + "' contains the " +
                                    "following unsupported keys: " + String.join(", ", givenKeys));
        }

        if (documentMap.containsKey(CONCEPTS)) {
            List<Map<String, Object>> concepts =
                // todo computeIfAbsend ersetzen
                (List<Map<String, Object>>) documentMap.computeIfAbsent(CONCEPTS, key -> emptyList());
            for (Map<String, Object> conceptSpec : concepts) {
                this.processConceptOrConstraint(conceptSpec, context,
                                                concept -> context.getBuilder().addConcept(concept),
                                                Concept.builder());
            }
        }

        if (documentMap.containsKey(CONSTRAINTS)) {
            List<Map<String, Object>> constraints =
                // todo computeIfAbsend ersetzen
                (List<Map<String, Object>>) documentMap.computeIfAbsent(CONSTRAINTS,
                                                                        key -> emptyList());
            for (Map<String, Object> constraintSpec : constraints) {
                this.processConceptOrConstraint(constraintSpec, context,
                                                constraint -> context.getBuilder().addConstraint(constraint),
                                                Constraint.builder());
            }
        }

        if (documentMap.containsKey(GROUPS)) {
            List<Map<String, Object>> groups =
                // todo computeIfAbsend ersetzen
                (List<Map<String, Object>>) documentMap.computeIfAbsent(GROUPS, key -> emptyList());
            for (Map<String, Object> group : groups) {
                processGroup(group, context);
            }
        }
    }

    private void processGroup(Map<String, Object> map, RuleContext context)
        throws RuleException {
        Set<String> givenKeys = map.keySet();
        boolean containsOnlyKnownKeys = GROUP_KEYS.containsAll(givenKeys);

        boolean requiredKeysPresent = givenKeys.containsAll(GROUP_KEYS_REQUIRED);

        if (!requiredKeysPresent) {
            List<String> missing = GROUP_KEYS_REQUIRED.stream()
                                                      .filter(key -> !givenKeys.contains(key))
                                                      .collect(toList());

            throw new RuleException("Rule source with id '" + context.getSource().getId() + "' " +
                                    "contains a group with the following missing keys: " +
                                    String.join(", ", missing));
        }

        if (!containsOnlyKnownKeys) {
            givenKeys.removeAll(GROUP_KEYS);

            throw new RuleException("Rule source with id '" + context.getSource().getId() + "' contains " +
                                    "a group containing the following unsupported keys: " +
                                    String.join(", ", givenKeys));
        }

        String id = (String)map.get(ID);
        // todo computeIfAbsend ersetzen
        List<Map<String, String>> concepts = (List<Map<String, String>>) map.computeIfAbsent(INCLUDES_CONCEPTS,
                                                                                             key -> emptyList());
        // todo computeIfAbsend ersetzen
        List<Map<String, String>> constraints = (List<Map<String, String>>) map.computeIfAbsent(INCLUDES_CONSTRAINTS,
                                                                                                key -> emptyList());
        // todo computeIfAbsend ersetzen
        List<Map<String, String>> groups = (List<Map<String, String>>) map.computeIfAbsent(INCLUDES_GROUPS,
                                                                                           key -> emptyList());

        Map<String, Severity> includedGroups = new HashMap<>();
        Map<String, Severity> includedConstraints = new HashMap<>();
        Map<String, Severity> includedConcepts = new HashMap<>();

        for (Map<String, String> refSpec : concepts) {
            Entry<String, Severity> entry = extractRuleReferencesFrom(id, refSpec, CONCEPT, context);
            includedConcepts.put(entry.getKey(), entry.getValue());
        }

        for (Map<String, String> refSpec : constraints) {
            Entry<String, Severity> entry = extractRuleReferencesFrom(id, refSpec, CONSTRAINT, context);
            includedConstraints.put(entry.getKey(), entry.getValue());
        }

        for (Map<String, String> refSpec : groups) {
            Entry<String, Severity> entry = extractRuleReferencesFrom(id, refSpec, GROUP, context);
            includedGroups.put(entry.getKey(), entry.getValue());
        }
        
        Group group = Group.builder().id(id).severity(null /* todo */)
                           .ruleSource(context.getSource())
                           .concepts(includedConcepts).constraints(includedConstraints)
                           .groups(includedGroups)
                           .build();

        context.getBuilder().addGroup(group);
    }

    private Map.Entry<String, Severity> extractRuleReferencesFrom(String containingRuleId, Map<String, String> refSpec,
                                                                  String role, RuleContext context)
        throws RuleException {

        Set<String> givenKeys = refSpec.keySet();

        boolean requiredKeysPresent = givenKeys.containsAll(RULE_REFERENCE_KEYS_REQUIRED);
        boolean unsupportedKeysGiven = givenKeys.stream().anyMatch(key -> !RULE_REFERENCE_KEYS.contains(key));

        if (!requiredKeysPresent) {
            List<String> missingKeys = new ArrayList<>(RULE_REFERENCE_KEYS_REQUIRED);
            missingKeys.removeAll(givenKeys);

            throw new RuleException("Rule source '" + context.getSource().getId() +
                                    "' contains the group '" + containingRuleId + "' with an " +
                                    "included " + role + " without the following required " +
                                    "keys: " + String.join(", ", missingKeys));
        }

        if (unsupportedKeysGiven) {
            List<String> unsupportedKeys = givenKeys.stream()
                                                    .filter(key -> !RULE_REFERENCE_KEYS.contains(key))
                                                    .collect(toList());

            throw new RuleException("Rule source '" + context.getSource().getId() +
                                        "' contains the group '" + containingRuleId +
                                        "' with an included " + role +
                                        " with the following unsupported keys: " +
                                        String.join(", ", unsupportedKeys));
        }

        String refId = refSpec.get(REF_ID);
        String severityVal = refSpec.get(SEVERITY);

        if (refSpec.containsKey(SEVERITY) && severityVal == null) {
            throw new RuleException("Rule source '" + context.getSource().getId() +
                                        "' contains the group '" + containingRuleId + "' with an " +
                                        "included " + role + " without specified value for its " +
                                        "severity");
        }

        // todo Wie ist hier das erwartete Verhalten?
        // Was ist, wenn keine Severity angegeben wurde
        Severity severity = severityVal == null ? null
                                                : toSeverity(severityVal, context);


        return new HashMap.SimpleEntry<>(refId, severity);
    }

    private <T extends AbstractExecutableRule, B extends AbstractExecutableRule.Builder<B, T>> void processConceptOrConstraint(Map<String, Object> map,
                                                                                                                               RuleContext context,
                                                                                                                               RuleConsumer<T> consumer,
                                                                                                                               B builder)
        throws RuleException {
        Set<String> givenKeys = map.keySet();
        boolean containsOnlyKnownKeys = CONCEPT_KEYS.containsAll(givenKeys);
        boolean requiredKeysPresent = givenKeys.containsAll(CONCEPT_KEYS_REQUIRED);

        if (!containsOnlyKnownKeys) {
            givenKeys.removeAll(CONCEPT_KEYS);

            throw new RuleException("Rule source '" + context.getSource().getId() + "' contains a concept with " +
                                        "one or more unknown keys: " + String.join(", ", givenKeys));
        }

        String id = (String) map.get(ID);
        String description = (String) map.get("description");
        String cypher = (String) map.get("cypher");
        String serverityV = (String) map.get("severity");
        String language = CYPHER; // todo? So richtig?

        CypherExecutable executable = new CypherExecutable(cypher);
        Map<String, Boolean> required = extractRequiredConcepts(map, id);
        Map<String, Parameter> parameters = extractParameters(map, id, context);
        Verification verification = extractVerifycation(map, id);

        // todo Add support for report section
        // Example: report.xml
        Report report = Report.builder().build();


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

    private Verification extractVerifycation(Map<String, Object> map, String conceptId) {
        Verification verification = null;

        if (map.containsKey(VERIFY)) {
            Map<String, Map<String, Object>> verify = (Map<String, Map<String, Object>>) map.get(VERIFY);

            if (verify.containsKey(AGGREGATION)) {
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
                Map<String, Object> config = verify.get("rowCount"); // todo Konstante

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

    private Map<String, Boolean> extractRequiredConcepts(Map<String, Object> map, String conceptId) {
        Map<String, Boolean> requiredConcepts = emptyMap();

        // todo Prüfung, ob nur die richtigen Keys da sind
        boolean hasRequiresSection = map.containsKey(REQUIRES_CONCEPTS);

        if (hasRequiresSection) {
            requiredConcepts = new HashMap<>();

            List<Map<String, Object>> list = (List<Map<String, Object>>)map.get(REQUIRES_CONCEPTS);

            for (Map<String, Object> required : list) {
                String refIdVal = (String) required.get("refId");
                Boolean optionalVal = (Boolean) required.get(OPTIONAL);

                Boolean aBoolean = ofNullable(optionalVal).orElse(Boolean.FALSE);

                requiredConcepts.put(refIdVal, aBoolean);
            }
        }

        return requiredConcepts;
    }

    private Map<String, Parameter> extractParameters(Map<String, Object> map, String conceptId,
                                                     RuleContext context) throws RuleException {
        Map<String, Parameter> parameters = emptyMap();

        boolean hasParameters = map.containsKey(REQUIRES_PARAMETERS);

        if (hasParameters) {
            // todo computeIfAbsend ersetzen
            List<Map<String, Object>> list = (List<Map<String, Object>>) map.computeIfAbsent(REQUIRES_PARAMETERS,
                                                                                             key -> emptyList());
            parameters = new HashMap<>();

            for (Map<String, Object> parameterSpec : list) {
                Set<String> givenKeys = parameterSpec.keySet();
                boolean mandatoryKeysGiven = givenKeys.containsAll(PARAMETER_KEYS_REQUIRED);
                boolean unsupportedKeysGiven = givenKeys.stream().anyMatch(key -> !PARAMETER_KEYS.contains(key));

                if (!mandatoryKeysGiven) {
                    Collection<String> missing = new ArrayList<>(PARAMETER_KEYS_REQUIRED);
                    missing.removeAll(givenKeys);

                    throw new RuleException("The concept '" + conceptId + "' in rule source '" +
                                            context.getSource().getId() + "' has an invalid parameter. The " +
                                            "following keys are missing: " + String.join(", ", missing));
                } else if (unsupportedKeysGiven) {
                    throw new RuleException("The concept '" + conceptId + "' in rule source '" +
                                            context.getSource().getId() + "' has an invalid parameter. The " +
                                            "following keys are not supported: " +
                                            givenKeys.stream().filter(key -> !PARAMETER_KEYS.contains(key))
                                                     .collect(Collectors.joining(", ")));
                }

                String nameVal = (String) parameterSpec.get(PARAMETER_NAME);
                String defaultVal = (String) parameterSpec.get(PARAMETER_DEFAULT_VALUE);
                String typeVal = (String) parameterSpec.get(PARAMETER_TYPE);

                if (nameVal == null || nameVal.isEmpty()) {
                    throw new RuleException("A parameter of concept '" + conceptId + "' " +
                                            "in rule source '" + context.getSource().getId() + "' has " +
                                            "no name");
                } else if (typeVal == null || typeVal.isEmpty()) {
                    throw new RuleException("The parameter '" + nameVal + "' of concept '" + conceptId + "' " +
                                            "in rule source '" + context.getSource().getId() + "' has no " +
                                            "parameter type specified");
                } else if (defaultVal == null && parameterSpec.containsKey(PARAMETER_DEFAULT_VALUE)) {
                    throw new RuleException("The parameter '" + nameVal + "' of concept '" + conceptId + "' " +
                                            "in rule source '" + context.getSource().getId() + "' has " +
                                            "no default value");
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
            throw new RuleException("'" + value + "' is not a supported type for a parameter");
        }
    }

    private Severity toSeverity(String value, RuleContext context) throws RuleException {
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
        void consume(T t) throws RuleHandlingException;
    }
}
