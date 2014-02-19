package com.buschmais.jqassistant.sonar.rule;

import com.buschmais.jqassistant.sonar.JQAssistant;
import org.sonar.api.resources.Java;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;

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
        return annotationRuleParser.parse(JQAssistant.KEY, RULE_CLASSES);
    }
}
