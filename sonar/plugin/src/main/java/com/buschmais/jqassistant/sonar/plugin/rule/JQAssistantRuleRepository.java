package com.buschmais.jqassistant.sonar.plugin.rule;

import java.util.List;
import java.util.Locale;

import org.sonar.api.rules.RulePriority;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
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
public final class JQAssistantRuleRepository implements RulesDefinition {

    @SuppressWarnings("rawtypes")
    public static final Class[] RULE_CLASSES = new Class[] {ConceptTemplateRule.class, ConstraintTemplateRule.class};

    @Override
    public void define(Context context) {
    	NewRepository newRepository = context.createRepository(JQAssistant.KEY, Java.KEY);
    	newRepository.setName(JQAssistant.NAME);
    	
    	createRules(newRepository);
    	RulesDefinitionAnnotationLoader annotationRuleParser = new RulesDefinitionAnnotationLoader();
    	annotationRuleParser.load(newRepository, RULE_CLASSES);
    	
    	newRepository.done();
    }
    
    public void createRules(NewRepository newRepository) {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(JQAssistantRuleRepository.class
                .getClassLoader());
        RulePluginRepository rulePluginRepository;
        try {
            rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot read rules.", e);
        }
        List<RuleSource> ruleSources = rulePluginRepository.getRuleSources();
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        RuleSetReader ruleSetReader = new XmlRuleSetReader();
        try {
            ruleSetReader.read(ruleSources, ruleSetBuilder);
        } catch (RuleException e) {
            throw new IllegalStateException("Cannot read rules", e);
        }
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();

        for (Concept concept : ruleSet.getConceptBucket().getAll()) {
        	createRule(newRepository, concept, RuleType.Concept);
        }

        for (Constraint constraint : ruleSet.getConstraintBucket().getAll()) {
        	createRule(newRepository, constraint, RuleType.Constraint);
        }
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
    private void createRule(NewRepository newRepository, ExecutableRule executableRule, RuleType ruleType) {
    	
    	NewRule rule = newRepository.createRule(executableRule.getId());
    	rule.setName(executableRule.getId());
        // set priority based on severity value
        rule.setSeverity(RulePriority.valueOf(executableRule.getSeverity().name()).name());
        rule.setMarkdownDescription(executableRule.getDescription());
        StringBuilder requiresConcepts = new StringBuilder();
        for (String requiredConcept : executableRule.getRequiresConcepts()) {
            if (requiresConcepts.length() > 0) {
                requiresConcepts.append(",");
            }
            requiresConcepts.append(requiredConcept);
        }
        createRuleParameter(rule, RuleParameter.Type, ruleType.name(), RuleParamType.STRING);
        
        rule.addTags(ruleType.name().toLowerCase(Locale.ENGLISH));
        createRuleParameter(rule, RuleParameter.Type, ruleType.name(), RuleParamType.STRING);
        if(requiresConcepts.length() > 0)
        {
        	createRuleParameter(rule, RuleParameter.RequiresConcepts, requiresConcepts.toString(), RuleParamType.STRING);
        }
        Executable executable = executableRule.getExecutable();
        String cypher = executable instanceof CypherExecutable ? ((CypherExecutable) executable).getStatement() : null;
        createRuleParameter(rule, RuleParameter.Cypher, cypher, RuleParamType.TEXT);
        Verification verification = executableRule.getVerification();
        if (verification instanceof AggregationVerification) {
            String aggregationColumn = ((AggregationVerification) verification).getColumn();
            createRuleParameter(rule, RuleParameter.Aggregation, Boolean.TRUE.toString(), RuleParamType.BOOLEAN);
            createRuleParameter(rule, RuleParameter.AggregationColumn, aggregationColumn, RuleParamType.STRING);
        }
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
    private void createRuleParameter(NewRule rule, RuleParameter ruleParameterDefinition, String value, RuleParamType valueType) {
    	NewParam parameter = rule.createParam(ruleParameterDefinition.getName());
        parameter.setDefaultValue(value);
        parameter.setType(valueType);
    }
}
