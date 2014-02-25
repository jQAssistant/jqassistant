package com.buschmais.jqassistant.sonar.rule;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
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
import java.util.Map;

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
        Map<String, Concept> concepts = ruleSet.getConcepts();
        for (Concept concept : concepts.values()) {
            Rule conceptRule = Rule.create(JQAssistant.KEY, concept.getId(), concept.getId());
            conceptRule.setDescription(concept.getDescription());
            RuleParam cypher = conceptRule.createParameter("cypher");
            cypher.setDefaultValue(concept.getQuery().getCypher());
            rules.add(conceptRule);
        }
        rules.addAll(annotationRuleParser.parse(JQAssistant.KEY, RULE_CLASSES));
        return rules;
    }
}
