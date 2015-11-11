package com.buschmais.jqassistant.sonar.plugin.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.java.Java;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.AggregationVerification;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.CypherExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Executable;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.Verification;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.XmlRuleSetReader;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.sonar.plugin.JQAssistant;

/**
 * The jQAssistant rule repository.
 * <p>
 * It provides two types of rules:
 * <ul>
 * <li>Rules from jQAssistant plugin descriptors which are deployed as extensions.</li>
 * <li>Template rules which can be configured by the user in the UI.</li>
 * </ul>
 */
public final class JQAssistantRuleRepository extends RuleRepository {

    @SuppressWarnings("rawtypes")
    public static final Collection<Class> RULE_CLASSES = Arrays.<Class>asList(ConceptTemplateRule.class, ConstraintTemplateRule.class);

    private final AnnotationRuleParser annotationRuleParser;

    /**
     * Constructor.
     * 
     * @param annotationRuleParser
     *            The {@link AnnotationRuleParser} to use for template rules.
     */
    public JQAssistantRuleRepository(AnnotationRuleParser annotationRuleParser) {
        super(JQAssistant.KEY, Java.KEY);
        setName(JQAssistant.NAME);
        this.annotationRuleParser = annotationRuleParser;
    }

    @Override
    public List<Rule> createRules() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(JQAssistantRuleRepository.class
                .getClassLoader());
        RulePluginRepository rulePluginRepository;
        try {
            rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new SonarException("Cannot read rules.", e);
        }
        List<RuleSource> ruleSources = rulePluginRepository.getRuleSources();
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        RuleSetReader ruleSetReader = new XmlRuleSetReader();
        RuleSet ruleSet;
        try {
            ruleSetReader.read(ruleSources, ruleSetBuilder);
        } catch (RuleException e) {
            throw new SonarException("Cannot read rules", e);
        }
        ruleSet = ruleSetBuilder.getRuleSet();
        List<Rule> rules = new ArrayList<>();

        for (Concept concept : ruleSet.getConceptBucket().getAll()) {
            rules.add(createRule(concept, RuleType.Concept));
        }

        for (Constraint constraint : ruleSet.getConstraintBucket().getAll()) {
            rules.add(createRule(constraint, RuleType.Constraint));
        }

        rules.addAll(annotationRuleParser.parse(JQAssistant.KEY, RULE_CLASSES));
        return rules;
    }

    /**
     * Create a rule from an executableRule.
     * 
     * @param executableRule
     *            The executableRule.
     * @param ruleType
     *            The rule type.
     * @return The rule.
     */
    private Rule createRule(ExecutableRule executableRule, RuleType ruleType) {
        Rule rule = Rule.create(JQAssistant.KEY, executableRule.getId(), executableRule.getId());
        rule.setDescription(executableRule.getDescription());
        // set priority based on severity value
        rule.setSeverity(RulePriority.valueOf(executableRule.getSeverity().name()));
        StringBuilder requiresConcepts = new StringBuilder();
        for (String requiredConcept : executableRule.getRequiresConcepts()) {
            if (requiresConcepts.length() > 0) {
                requiresConcepts.append(",");
            }
            requiresConcepts.append(requiredConcept);
        }
        createRuleParameter(rule, RuleParameter.Type, ruleType.name());
        createRuleParameter(rule, RuleParameter.RequiresConcepts, requiresConcepts.toString());
        Executable executable = executableRule.getExecutable();
        String cypher = executable instanceof CypherExecutable ? ((CypherExecutable) executable).getStatement() : null;
        createRuleParameter(rule, RuleParameter.Cypher, cypher);
        Verification verification = executableRule.getVerification();
        if (verification instanceof AggregationVerification) {
            String aggregationColumn = ((AggregationVerification) verification).getColumn();
            createRuleParameter(rule, RuleParameter.Aggregation, Boolean.TRUE.toString());
            createRuleParameter(rule, RuleParameter.AggregationColumn, aggregationColumn);
        }
        return rule;
    }

    /**
     * Create a rule parameter.
     * 
     * @param rule
     *            The rule.
     * @param ruleParameterDefinition
     *            The parameter name.
     * @param value
     *            The default value.
     * @return The parameter.
     */
    private RuleParam createRuleParameter(Rule rule, RuleParameter ruleParameterDefinition, String value) {
        RuleParam parameter = rule.createParameter(ruleParameterDefinition.getName());
        parameter.setDefaultValue(value);
        return parameter;
    }
}
