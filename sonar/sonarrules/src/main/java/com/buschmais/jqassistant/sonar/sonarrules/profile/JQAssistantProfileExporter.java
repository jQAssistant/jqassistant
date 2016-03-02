package com.buschmais.jqassistant.sonar.sonarrules.profile;

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
import org.sonar.plugins.java.Java;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetWriter;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.AggregationVerification;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.CypherExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Report;
import com.buschmais.jqassistant.core.analysis.api.rule.RowCountVerification;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.api.rule.Verification;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetWriterImpl;
import com.buschmais.jqassistant.sonar.plugin.JQAssistant;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantRuleType;
import com.buschmais.jqassistant.sonar.sonarrules.rule.AbstractTemplateRule;
import com.buschmais.jqassistant.sonar.sonarrules.rule.ConceptTemplateRule;
import com.buschmais.jqassistant.sonar.sonarrules.rule.ConstraintTemplateRule;
import com.buschmais.jqassistant.sonar.sonarrules.rule.JQAssistantRuleRepository;
import com.buschmais.jqassistant.sonar.sonarrules.rule.RuleParameter;

/**
 * A {@link ProfileExporter} implementation which provides rules as permalink for direct usage by the jQAssistant analyzer.
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
        setSupportedLanguages(Java.KEY);
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {
        @SuppressWarnings("unchecked")
        CheckFactory<AbstractTemplateRule> annotationCheckFactory = AnnotationCheckFactory.create(profile, JQAssistant.KEY,
                Arrays.asList(JQAssistantRuleRepository.RULE_CLASSES));
        Map<String, Concept> concepts = new HashMap<>();
        Map<String, Severity> conceptSeverities = new HashMap<>();
        Map<String, Severity> constraintSeverities = new HashMap<>();
        RuleSetBuilder builder = RuleSetBuilder.newInstance();
        Map<ExecutableRule, Set<String>> executables = new HashMap<>();
        try {
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
                    builder.addConcept((Concept) executable);
                    concepts.put(executable.getId(), (Concept) executable);
                    conceptSeverities.put(executable.getId(), executable.getSeverity());
                } else if (executable instanceof Constraint) {
                    builder.addConstraint((Constraint) executable);
                    constraintSeverities.put(executable.getId(), executable.getSeverity());
                }
            }
            for (Set<String> requiredConcepts : executables.values()) {
                resolveRequiredConcepts(requiredConcepts, concepts);
            }
            Group group = Group.Builder.newGroup().groupId(profile.getName()).conceptIds(conceptSeverities).constraintIds(constraintSeverities).get();
            builder.addGroup(group);
            RuleSet ruleSet = builder.getRuleSet();
            RuleSetWriter ruleSetWriter = new RuleSetWriterImpl();
            LOGGER.debug("Exporting rule set " + ruleSet.toString());
            ruleSetWriter.write(ruleSet, writer);
        } catch (RuleException e) {
            throw new IllegalStateException("Cannot export rules.", e);
        }
    }

    private Set<String> getRequiresConcepts(String requiresConcepts) {
    	if(requiresConcepts == null)
    		return Collections.emptySet();
        return new HashSet<>(Arrays.asList(StringUtils.splitByWholeSeparator(requiresConcepts, ",")));
    }

    /**
     * Resolves and adds required concepts for an executable.
     * 
     * @param requiresConcepts
     *            The string containing the comma separated is of required concepts.
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
            throw new IllegalStateException("Cannot determine cypher for " + rule);
        }
        String cypher = cypherParam.getDefaultValue();
        RuleParam requiresConceptsParam = rule.getParam(RuleParameter.RequiresConcepts.getName());
        Set<String> requiresConcepts = requiresConceptsParam != null ? getRequiresConcepts(requiresConceptsParam.getDefaultValue()) : Collections
                .<String>emptySet();
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
            throw new IllegalStateException("Cannot determine type of rule for " + rule);
        }
        AbstractExecutableRule executable;
        String type = typeParam.getDefaultValue();
        JQAssistantRuleType ruleType = JQAssistantRuleType.valueOf(type);
        String id = rule.getName();
        String description = rule.getDescription();
        Severity severity = Severity.valueOf(rule.getSeverity().name());
        RuleParam aggregationParam = rule.getParam(RuleParameter.Aggregation.getName());
        boolean aggregation = aggregationParam != null ? aggregationParam.getDefaultValueAsBoolean() : false;
        Verification verification;
        if (aggregation) {
            RuleParam aggregationColumnParam = rule.getParam(RuleParameter.AggregationColumn.getName());
            verification = new AggregationVerification(aggregationColumnParam != null ? aggregationColumnParam.getDefaultValue() : null);
        } else {
            verification = new RowCountVerification();
        }
        RuleParam primaryReportColumnParam = rule.getParam(RuleParameter.PrimaryReportColumn.getName());
        String primaryReportColumn = primaryReportColumnParam != null ? primaryReportColumnParam.getDefaultValue() : null;
        Report report = new Report("default", primaryReportColumn, new Properties());
        switch (ruleType) {
            case Concept:
            	executable = Concept.Builder.newConcept().id(id).description(description).severity(severity).executable(new CypherExecutable(cypher)).requiresConceptIds(requiresConcepts).verification(verification).report(report).get();
                break;
            case Constraint:
            	executable = Constraint.Builder.newConstraint().id(id).description(description).severity(severity).executable(new CypherExecutable(cypher)).requiresConceptIds(requiresConcepts).verification(verification).report(report).get();
                break;
            default:
                throw new IllegalStateException("Rule type is not supported " + ruleType);
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
        Verification verification;
        if (check.isAggregation()) {
            verification = new AggregationVerification(check.getAggregationColumn());
        } else {
            verification = new RowCountVerification();
        }
        Set<String> requiresConcepts = getRequiresConcepts(check.getRequiresConcepts());
        Report report = new Report("default", check.getPrimaryReportColumn(), new Properties());
        if (check instanceof ConceptTemplateRule) {
        	executable = Concept.Builder.newConcept().id(id).description(description).severity(severity).executable(new CypherExecutable(cypher)).requiresConceptIds(requiresConcepts).verification(verification).report(report).get();
        } else if (check instanceof ConstraintTemplateRule) {
        	executable = Constraint.Builder.newConstraint().id(id).description(description).severity(severity).executable(new CypherExecutable(cypher)).requiresConceptIds(requiresConcepts).verification(verification).report(report).get();
        } else {
            throw new IllegalStateException("Unknown type " + check.getClass());
        }
        return executable;
    }
}
