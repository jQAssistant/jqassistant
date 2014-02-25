package com.buschmais.jqassistant.sonar.profile;

import com.buschmais.jqassistant.core.analysis.api.RuleSetWriter;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetWriterImpl;
import com.buschmais.jqassistant.sonar.JQAssistant;
import com.buschmais.jqassistant.sonar.rule.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.checks.CheckFactory;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.utils.SonarException;

import java.io.Writer;
import java.util.*;

public class JQAssistantProfileExporter extends ProfileExporter {

    private final static Logger LOGGER = LoggerFactory.getLogger(JQAssistantProfileExporter.class);

    private final RuleFinder ruleFinder;

    public JQAssistantProfileExporter(RuleFinder ruleFinder) {
        super(JQAssistant.KEY, JQAssistant.NAME);
        this.ruleFinder = ruleFinder;
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {
        CheckFactory<AbstractTemplateRule> annotationCheckFactory = AnnotationCheckFactory.<AbstractTemplateRule>create(profile, JQAssistant.KEY, JQAssistantRuleRepository.RULE_CLASSES);
        Map<String, Concept> concepts = new HashMap<String, Concept>();
        Map<AbstractExecutable, Set<String>> executables = new HashMap<AbstractExecutable, Set<String>>();
        for (ActiveRule activeRule : profile.getActiveRulesByRepository(JQAssistant.KEY)) {
            AbstractTemplateRule check = annotationCheckFactory.getCheck(activeRule);
            AbstractExecutable executable;
            String requiresConcepts;
            if (check == null) {
                executable = createExecutableFromActiveRule(activeRule);
                requiresConcepts = activeRule.getParameter(RuleParameter.RequiresConcepts.getName());
            } else {
                executable = createExecutableFromTemplate(activeRule, check);
                requiresConcepts = check.getRequiresConcepts();
            }
            List<String> requiresConceptsArray = requiresConcepts != null ? Arrays.asList(StringUtils.splitByWholeSeparator(requiresConcepts, ",")) : Collections.<String>emptyList();
            executables.put(executable, new HashSet<String>(requiresConceptsArray));
            if (executable instanceof Concept) {
                concepts.put(executable.getId(), (Concept) executable);
            }
        }
        Group group = new Group();
        group.setId(profile.getName());
        for (Map.Entry<AbstractExecutable, Set<String>> executableEntry : executables.entrySet()) {
            AbstractExecutable executable = executableEntry.getKey();
            Set<String> requiresConcepts = executableEntry.getValue();
            addRequiredConcepts(requiresConcepts, group, concepts);
            if (executable instanceof Concept) {
                group.getConcepts().add((Concept) executable);
            } else if (executable instanceof Constraint) {
                group.getConstraints().add((Constraint) executable);
            }
        }
        RuleSet ruleSet = new RuleSet();
        ruleSet.getGroups().put(group.getId(), group);
        RuleSetWriter ruleSetWriter = new RuleSetWriterImpl();
        LOGGER.info("Exporting rule set " + ruleSet.toString());
        ruleSetWriter.write(ruleSet, writer);
    }

    private void addRequiredConcepts(Set<String> requiresConcepts, Group group, Map<String, Concept> concepts) {
        for (String requiresConcept : requiresConcepts) {
            Concept requiredConcept = concepts.get(requiresConcept);
            if (requiredConcept == null) {
                Rule rule = ruleFinder.findByKey(JQAssistant.KEY, requiresConcept);
                requiredConcept = (Concept) createExecutableFromRule(rule);
                group.getConcepts().add(requiredConcept);
            }
        }
    }

    /**
     * Creates an executable from an active rule and its parameters.
     *
     * @param activeRule The active rule.
     * @return The executable.
     */
    private AbstractExecutable createExecutableFromActiveRule(ActiveRule activeRule) {
        String cypher = activeRule.getParameter(RuleParameter.Cypher.getName());
        Rule rule = activeRule.getRule();
        return createExecutableFromRule(rule, cypher);
    }


    /**
     * Creates an executable from a rule.
     *
     * @param rule The rule.
     * @return The executable.
     */
    private AbstractExecutable createExecutableFromRule(Rule rule) {
        RuleParam cypherParam = rule.getParam(RuleParameter.Type.getName());
        if (cypherParam == null) {
            throw new SonarException("Cannot determine cypher for " + rule);
        }
        String cypher = cypherParam.getDefaultValue();
        return createExecutableFromRule(rule, cypher);
    }

    /**
     * Creates an executable from a rule.
     *
     * @param rule   The rule.
     * @param cypher The cypher expression.
     * @return The executable.
     */
    private AbstractExecutable createExecutableFromRule(Rule rule, String cypher) {
        RuleParam typeParam = rule.getParam(RuleParameter.Type.getName());
        if (typeParam == null) {
            throw new SonarException("Cannot determine type of rule for " + rule);
        }
        AbstractExecutable executable;
        String type = typeParam.getDefaultValue();
        RuleType ruleType = RuleType.valueOf(type);
        switch (ruleType) {
            case Concept:
                executable = new Concept();
                break;
            case Constraint:
                executable = new Constraint();
                break;
            default:
                throw new SonarException("Rule type is not supported " + ruleType);
        }
        createExecutable(executable, rule.getName(), rule.getDescription(), cypher);
        return executable;
    }

    /**
     * Creates an executable from a check based on a template.
     *
     * @param activeRule The active rule.
     * @param check      The check.
     * @return The executable.
     */
    private AbstractExecutable createExecutableFromTemplate(ActiveRule activeRule, AbstractTemplateRule check) {
        AbstractExecutable executable;
        if (check instanceof ConceptTemplateRule) {
            executable = new Concept();
        } else if (check instanceof ConstraintTemplateRule) {
            executable = new Constraint();
        } else {
            throw new SonarException("Unknown type " + check.getClass());
        }
        createExecutable(executable, activeRule.getRule().getName(), activeRule.getRule().getDescription(), check.getCypher());
        return executable;
    }

    private void createExecutable(AbstractExecutable executable, String id, String description, String cypher) {
        executable.setId(id);
        executable.setDescription(description);
        Query query = new Query();
        query.setCypher(cypher);
        executable.setQuery(query);
    }
}
