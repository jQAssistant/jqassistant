package com.buschmais.jqassistant.sonar.plugin.profile;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

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

import com.buschmais.jqassistant.core.analysis.api.RuleSetWriter;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetWriterImpl;
import com.buschmais.jqassistant.sonar.plugin.JQAssistant;
import com.buschmais.jqassistant.sonar.plugin.rule.*;

/**
 * A {@link ProfileExporter} implementation which provides rules as permalink
 * for direct usage by the jQAssistant analyzer.
 */
public class JQAssistantProfileExporter extends ProfileExporter {

    private final static Logger LOGGER = LoggerFactory.getLogger(JQAssistantProfileExporter.class);

    private final RuleFinder ruleFinder;

    /**
     * Constructor.
     * 
     * @param ruleFinder
     *            The {@link org.sonar.api.rules.RuleFinder} to use.
     */
    public JQAssistantProfileExporter(RuleFinder ruleFinder) {
        super(JQAssistant.KEY, JQAssistant.NAME);
        this.ruleFinder = ruleFinder;
        super.setMimeType("application/xml");
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {
        @SuppressWarnings("unchecked")
        CheckFactory<AbstractTemplateRule> annotationCheckFactory = AnnotationCheckFactory.create(profile, JQAssistant.KEY,
                JQAssistantRuleRepository.RULE_CLASSES);
        Map<String, Concept> concepts = new HashMap<String, Concept>();
        Map<AbstractRule, String> executables = new HashMap<AbstractRule, String>();
        for (ActiveRule activeRule : profile.getActiveRulesByRepository(JQAssistant.KEY)) {
            AbstractTemplateRule check = annotationCheckFactory.getCheck(activeRule);
            AbstractRule executable;
            String requiresConcepts;
            if (check == null) {
                executable = createExecutableFromActiveRule(activeRule);
                requiresConcepts = activeRule.getParameter(RuleParameter.RequiresConcepts.getName());
            } else {
                executable = createExecutableFromTemplate(activeRule, check);
                requiresConcepts = check.getRequiresConcepts();
            }
            executables.put(executable, requiresConcepts);
            if (executable instanceof Concept) {
                concepts.put(executable.getId(), (Concept) executable);
            }
        }
        Group group = new Group();
        group.setId(profile.getName());
        for (Map.Entry<AbstractRule, String> executableEntry : executables.entrySet()) {
            AbstractRule executable = executableEntry.getKey();
            String requiresConcepts = executableEntry.getValue();
            addRequiredConcepts(executable, requiresConcepts, concepts);
            if (executable instanceof Concept) {
                group.getConcepts().add((Concept) executable);
            } else if (executable instanceof Constraint) {
                group.getConstraints().add((Constraint) executable);
            }
        }
        RuleSet ruleSet = new RuleSet();
        ruleSet.getGroups().put(group.getId(), group);
        RuleSetWriter ruleSetWriter = new RuleSetWriterImpl();
        LOGGER.debug("Exporting rule set " + ruleSet.toString());
        ruleSetWriter.write(ruleSet, writer);
    }

    /**
     * Resolves and adds required concepts for an executable.
     * 
     * @param executable
     *            The executable.
     * @param requiresConcepts
     *            The string containing the comma separated is of required
     *            concepts.
     * @param concepts
     *            The map of already resolved concepts.
     */
    private void addRequiredConcepts(AbstractRule executable, String requiresConcepts, Map<String, Concept> concepts) {
        LOGGER.debug("Adding required concepts for " + executable.getId());
        if (!StringUtils.isEmpty(requiresConcepts)) {
            for (String requiresConceptId : StringUtils.splitByWholeSeparator(requiresConcepts, ",")) {
                LOGGER.debug("Required concept: " + requiresConceptId);
                Concept requiredConcept = concepts.get(requiresConceptId);
                if (requiredConcept == null) {
                    LOGGER.debug("Finding rule for concept : " + requiresConceptId);
                    Rule rule = ruleFinder.findByKey(JQAssistant.KEY, requiresConceptId);
                    requiredConcept = (Concept) createExecutableFromRule(rule);
                    concepts.put(requiresConceptId, requiredConcept);
                    RuleParam requiresConceptsParam = rule.getParam(RuleParameter.RequiresConcepts.getName());
                    if (requiresConceptsParam != null) {
                        addRequiredConcepts(requiredConcept, requiresConceptsParam.getDefaultValue(), concepts);
                    }
                }
                if (requiredConcept != null) {
                    LOGGER.debug("Adding required concept with id " + requiresConceptId + " to " + executable.getId());
                    executable.getRequiresConcepts().add(requiredConcept);
                } else {
                    LOGGER.warn("Cannot resolve required concept with id " + requiresConceptId);
                }
            }
        }
    }

    /**
     * Creates an executable from an active rule and its parameters.
     * 
     * @param activeRule
     *            The active rule.
     * @return The executable.
     */
    private AbstractRule createExecutableFromActiveRule(ActiveRule activeRule) {
        String cypher = activeRule.getParameter(RuleParameter.Cypher.getName());
        Rule rule = activeRule.getRule();
        return createExecutableFromRule(rule, cypher);
    }

    /**
     * Creates an executable from a rule.
     * 
     * @param rule
     *            The rule.
     * @return The executable.
     */
    private AbstractRule createExecutableFromRule(Rule rule) {
        RuleParam cypherParam = rule.getParam(RuleParameter.Cypher.getName());
        if (cypherParam == null) {
            throw new SonarException("Cannot determine cypher for " + rule);
        }
        String cypher = cypherParam.getDefaultValue();
        return createExecutableFromRule(rule, cypher);
    }

    /**
     * Creates an executable from a rule.
     * 
     * @param rule
     *            The rule.
     * @param cypher
     *            The cypher expression.
     * @return The executable.
     */
    private AbstractRule createExecutableFromRule(Rule rule, String cypher) {
        RuleParam typeParam = rule.getParam(RuleParameter.Type.getName());
        if (typeParam == null) {
            throw new SonarException("Cannot determine type of rule for " + rule);
        }
        AbstractRule executable;
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
     * @param activeRule
     *            The active rule.
     * @param check
     *            The check.
     * @return The executable.
     */
    private AbstractRule createExecutableFromTemplate(ActiveRule activeRule, AbstractTemplateRule check) {
        AbstractRule executable;
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

    /**
     * Sets the given parameters for an executable.
     * 
     * @param executable
     *            The executable.
     * @param id
     *            The id.
     * @param description
     *            The description.
     * @param cypher
     *            The cypher expression.
     */
    private void createExecutable(AbstractRule executable, String id, String description, String cypher) {
        executable.setId(id);
        executable.setDescription(description);
        Query query = new Query();
        query.setCypher(cypher);
        executable.setQuery(query);
    }
}
