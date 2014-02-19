package com.buschmais.jqassistant.sonar.profile;

import com.buschmais.jqassistant.core.analysis.api.RuleSetWriter;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetWriterImpl;
import com.buschmais.jqassistant.sonar.JQAssistant;
import com.buschmais.jqassistant.sonar.rule.AbstractTemplateRule;
import com.buschmais.jqassistant.sonar.rule.ConceptTemplateRule;
import com.buschmais.jqassistant.sonar.rule.ConstraintTemplateRule;
import com.buschmais.jqassistant.sonar.rule.JQAssistantRuleRepository;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.checks.CheckFactory;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.SonarException;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class JQAssistantProfileExporter extends ProfileExporter {

    public JQAssistantProfileExporter() {
        super(JQAssistant.KEY, JQAssistant.NAME);
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {
        CheckFactory<AbstractTemplateRule> annotationCheckFactory = AnnotationCheckFactory.<AbstractTemplateRule>create(profile, JQAssistant.KEY, JQAssistantRuleRepository.RULE_CLASSES);
        Map<String, Concept> concepts = new HashMap<String, Concept>();
        Map<String, Constraint> constraints = new HashMap<String, Constraint>();
        for (ActiveRule activeRule : profile.getActiveRulesByRepository(JQAssistant.KEY)) {
            AbstractTemplateRule check = annotationCheckFactory.getCheck(activeRule);
            if (check instanceof ConceptTemplateRule) {
                Concept concept = new Concept();
                createExecutable(activeRule, check, concept);
                concepts.put(concept.getId(), concept);
            } else if (check instanceof ConstraintTemplateRule) {
                Constraint constraint = new Constraint();
                createExecutable(activeRule, check, constraint);
                constraints.put(constraint.getId(), constraint);
            } else {
                throw new SonarException("Unknown type " + check.getClass());
            }
        }
        Group group = new Group();
        group.setId(profile.getName());
        for (ActiveRule activeRule : profile.getActiveRulesByRepository(JQAssistant.KEY)) {
            AbstractTemplateRule check = annotationCheckFactory.getCheck(activeRule);
            String name = activeRule.getRule().getName();
            if (check instanceof ConceptTemplateRule) {
                Concept concept = concepts.get(name);
                addRequiredConcepts(concept, check, concepts);
                group.getConcepts().add(concept);
            } else if (check instanceof ConstraintTemplateRule) {
                Constraint constraint = constraints.get(name);
                addRequiredConcepts(constraint, check, concepts);
                group.getConstraints().add(constraint);
            } else {
                throw new SonarException("Unknown type " + check.getClass());
            }
        }
        RuleSet ruleSet = new RuleSet();
        ruleSet.getGroups().put(group.getId(), group);
        RuleSetWriter ruleSetWriter = new RuleSetWriterImpl();
        ruleSetWriter.write(ruleSet, writer);
    }

    private void addRequiredConcepts(AbstractExecutable executable, AbstractTemplateRule check, Map<String, Concept> concepts) {
        String requiresConcepts = check.getRequiresConcepts();
        if (!StringUtils.isEmpty(requiresConcepts)) {
            for (String requiresConcept : StringUtils.splitByWholeSeparator(requiresConcepts, ",")) {
                executable.getRequiredConcepts().add(concepts.get(requiresConcept));
            }
        }
    }

    private void createExecutable(ActiveRule activeRule, AbstractTemplateRule check, AbstractExecutable executable) {
        executable.setId(activeRule.getRule().getName());
        executable.setDescription(activeRule.getRule().getDescription());
        Query query = new Query();
        query.setCypher(check.getCypher());
        executable.setQuery(query);
    }
}
