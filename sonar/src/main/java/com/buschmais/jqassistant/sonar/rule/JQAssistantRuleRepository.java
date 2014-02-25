package com.buschmais.jqassistant.sonar.rule;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.pluginmanager.api.PluginManager;
import com.buschmais.jqassistant.core.pluginmanager.impl.PluginManagerImpl;
import com.buschmais.jqassistant.sonar.JQAssistant;
import org.sonar.api.resources.Java;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.rules.RuleRepository;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class JQAssistantRuleRepository extends RuleRepository {

    public static final List<Class> RULE_CLASSES = Arrays.<Class>asList(ConceptTemplateRule.class, ConstraintTemplateRule.class);

    private final AnnotationRuleParser annotationRuleParser;

    public JQAssistantRuleRepository(AnnotationRuleParser annotationRuleParser) {
        super(JQAssistant.KEY, Java.KEY);
        setName(JQAssistant.NAME);
        this.annotationRuleParser = annotationRuleParser;
    }

    @Override
    public List<Rule> createRules() {
        List<Rule> rules = new ArrayList<Rule>();
        PluginManager pluginManager = new PluginManagerImpl();
        List<Source> ruleSources = pluginManager.getRuleSources();
        RuleSetReader ruleSetReader = new RuleSetReaderImpl();
        RuleSet ruleSet = ruleSetReader.read(ruleSources);
        for (Concept concept : ruleSet.getConcepts().values()) {
            rules.add(createRule(concept, RuleType.Concept));
        }
        for (Constraint constraint : ruleSet.getConstraints().values()) {
            rules.add(createRule(constraint, RuleType.Constraint));
        }
        rules.addAll(annotationRuleParser.parse(JQAssistant.KEY, RULE_CLASSES));
        return rules;
    }

    /**
     * Create a rule from an executable.
     *
     * @param executable The executable.
     * @param ruleType   The rule type.
     * @return The rule.
     */
    private Rule createRule(AbstractExecutable executable, RuleType ruleType) {
        Rule rule = Rule.create(JQAssistant.KEY, executable.getId(), executable.getId());
        rule.setDescription(executable.getDescription());
        StringBuilder requiresConcepts = new StringBuilder();
        for (Concept requiredConcept : executable.getRequiredConcepts()) {
            if (requiresConcepts.length() > 0) {
                requiresConcepts.append(",");
            }
            requiresConcepts.append(requiredConcept.getId());
        }
        createRuleParameter(rule, RuleParameter.Type, ruleType.name());
        createRuleParameter(rule, RuleParameter.RequiresConcepts, requiresConcepts.toString());
        createRuleParameter(rule, RuleParameter.Cypher, executable.getQuery().getCypher());
        return rule;
    }

    /**
     * Create a rule parameter.
     *
     * @param rule                    The rule.
     * @param ruleParameterDefinition The parameter name.
     * @param value                   The default value.
     * @return The parameter.
     */
    private RuleParam createRuleParameter(Rule rule, RuleParameter ruleParameterDefinition, String value) {
        RuleParam parameter = rule.createParameter(ruleParameterDefinition.getName());
        parameter.setDefaultValue(value);
        return parameter;
    }
}
