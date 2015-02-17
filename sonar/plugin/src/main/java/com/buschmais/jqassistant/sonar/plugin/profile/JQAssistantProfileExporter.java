package com.buschmais.jqassistant.sonar.plugin.profile;

import java.io.Writer;
import java.util.*;

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

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
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
        Map<String, Concept> concepts = new HashMap<>();
        Map<String, Severity> conceptsOfGroup = new HashMap<>();
        Map<String, Severity> constraintsOfGroup = new HashMap<>();
        Map<String, Constraint> constraints = new HashMap<>();
        Map<ExecutableRule, Set<String>> executables = new HashMap<>();
        for (ActiveRule activeRule : profile.getActiveRulesByRepository(JQAssistant.KEY)) {
            AbstractTemplateRule check = annotationCheckFactory.getCheck(activeRule);
            AbstractExecutableRule executable;
            if (check == null) {
                executable = createExecutableFromActiveRule(activeRule);
            } else {
                executable = createExecutableFromTemplate(activeRule, check);
            }
            Set<String> requiresConcepts = executable.getRequiresConcepts();
            executables.put(executable, requiresConcepts);
            if (executable instanceof Concept) {
                concepts.put(executable.getId(), (Concept) executable);
                conceptsOfGroup.put(executable.getId(), executable.getSeverity());
            } else if (executable instanceof Constraint) {
                constraints.put(executable.getId(), (Constraint) executable);
                constraintsOfGroup.put(executable.getId(), executable.getSeverity());
            }
        }
        for (Set<String> requiredConcepts : executables.values()) {
            resolveRequiredConcepts(requiredConcepts, concepts);
        }
        Group group = new Group(profile.getName(), null, conceptsOfGroup, constraintsOfGroup, Collections.<String> emptySet());
        Map<String, Group> groups = new HashMap<>();
        groups.put(group.getId(), group);
        RuleSet ruleSet = new DefaultRuleSet(Collections.<String, Template> emptyMap(), concepts, constraints, groups,
                Collections.<String, MetricGroup> emptyMap());
        RuleSetWriter ruleSetWriter = new RuleSetWriterImpl();
        LOGGER.debug("Exporting rule set " + ruleSet.toString());
        try {
            ruleSetWriter.write(ruleSet, writer);
        } catch (AnalysisException e) {
            throw new SonarException("Cannot export rule set.", e);
        }
    }

    private Set<String> getRequiresConcepts(String requiresConcepts) {
        return new HashSet<>(Arrays.asList(StringUtils.splitByWholeSeparator(requiresConcepts, ",")));
    }

    /**
     * Resolves and adds required concepts for an executable.
     * 
     * @param requiresConcepts
     *            The string containing the comma separated is of required
     *            concepts.
     */
    private Set<String> getRequiredConcepts(String requiresConcepts) {
        Set<String> result = new HashSet<>();
        if (!StringUtils.isEmpty(requiresConcepts)) {
            for (String requiresConceptId : StringUtils.splitByWholeSeparator(requiresConcepts, ",")) {
                result.add(requiresConceptId);
            }
        }
        return result;
    }

    private void resolveRequiredConcepts(Set<String> requiredConcepts, Map<String, Concept> concepts) {
        for (String requiredConcept : requiredConcepts) {
            resolveRequiredConcepts(requiredConcept, concepts);
        }
    }

    private void resolveRequiredConcepts(String requiredConceptId, Map<String, Concept> concepts) {
        LOGGER.debug("Required concept: " + requiredConceptId);
        Concept requiredConcept = concepts.get(requiredConceptId);
        if (requiredConcept == null) {
            LOGGER.debug("Finding rule for concept : " + requiredConceptId);
            Rule rule = ruleFinder.findByKey(JQAssistant.KEY, requiredConceptId);
            requiredConcept = (Concept) createExecutableFromRule(rule);
            concepts.put(requiredConceptId, requiredConcept);
            RuleParam requiresConceptsParam = rule.getParam(RuleParameter.RequiresConcepts.getName());
            if (requiresConceptsParam != null) {
                Set<String> requiredConcepts = getRequiredConcepts(requiresConceptsParam.getDefaultValue());
                resolveRequiredConcepts(requiredConcepts, concepts);
            }
        }
        if (requiredConcept != null) {
            LOGGER.debug("Adding required concept with id " + requiredConceptId);
        } else {
            LOGGER.warn("Cannot resolve required concept with id " + requiredConceptId);
        }
    }

    /**
     * Creates an executable from an active rule and its parameters.
     * 
     * @param activeRule
     *            The active rule.
     * @return The executable.
     */
    private AbstractExecutableRule createExecutableFromActiveRule(ActiveRule activeRule) {
        return createExecutableFromRule(activeRule.getRule());
    }

    /**
     * Creates an executable from a rule.
     * 
     * @param rule
     *            The rule.
     * @return The executable.
     */
    private AbstractExecutableRule createExecutableFromRule(Rule rule) {
        RuleParam cypherParam = rule.getParam(RuleParameter.Cypher.getName());
        if (cypherParam == null) {
            throw new SonarException("Cannot determine cypher for " + rule);
        }
        String cypher = cypherParam.getDefaultValue();
        RuleParam requiresConceptsParam = rule.getParam(RuleParameter.RequiresConcepts.getName());
        Set<String> requiresConcepts = requiresConceptsParam != null ? getRequiresConcepts(requiresConceptsParam.getDefaultValue()) : Collections
                .<String> emptySet();
        return createExecutableFromRule(rule, cypher, requiresConcepts);
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
    private AbstractExecutableRule createExecutableFromRule(Rule rule, String cypher, Set<String> requiresConcepts) {
        RuleParam typeParam = rule.getParam(RuleParameter.Type.getName());
        if (typeParam == null) {
            throw new SonarException("Cannot determine type of rule for " + rule);
        }
        AbstractExecutableRule executable;
        String type = typeParam.getDefaultValue();
        RuleType ruleType = RuleType.valueOf(type);
        String id = rule.getName();
        String description = rule.getDescription();
        Severity severity = Severity.valueOf(rule.getSeverity().name());
        switch (ruleType) {
        case Concept:
            executable = new Concept(id, description, severity, null, cypher, null, null, null, requiresConcepts);
            break;
        case Constraint:
            executable = new Constraint(id, description, severity, null, cypher, null, null, null, requiresConcepts);
            break;
        default:
            throw new SonarException("Rule type is not supported " + ruleType);
        }
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
    private AbstractExecutableRule createExecutableFromTemplate(ActiveRule activeRule, AbstractTemplateRule check) {
        AbstractExecutableRule executable;
        String id = activeRule.getRule().getName();
        String description = activeRule.getRule().getDescription();
        Severity severity = Severity.valueOf(activeRule.getSeverity().name());
        String cypher = check.getCypher();
        Set<String> requiresConcepts = getRequiresConcepts(check.getRequiresConcepts());
        if (check instanceof ConceptTemplateRule) {
            executable = new Concept(id, description, severity, null, cypher, null, null, null, requiresConcepts);
        } else if (check instanceof ConstraintTemplateRule) {
            executable = new Constraint(id, description, severity, null, cypher, null, null, null, requiresConcepts);
        } else {
            throw new SonarException("Unknown type " + check.getClass());
        }
        return executable;
    }

}
