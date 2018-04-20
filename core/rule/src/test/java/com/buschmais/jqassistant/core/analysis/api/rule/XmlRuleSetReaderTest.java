package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class XmlRuleSetReaderTest {

    @Test
    public void readScriptRule() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/javascript-rules.xml");
        ConceptBucket conceptBucket = ruleSet.getConceptBucket();
        assertThat(conceptBucket.size(), equalTo(2));
        assertThat(conceptBucket.getIds(), hasItems("test:JavaScriptConcept", "test:JavaScriptExecutableConcept"));
        Collection<? extends AbstractRule> all = conceptBucket.getAll();
        verifyExecutableRule(all);
        ConstraintBucket constraintBucket = ruleSet.getConstraintBucket();
        assertThat(constraintBucket.size(), equalTo(2));
        assertThat(constraintBucket.getIds(), hasItems("test:JavaScriptConstraint", "test:JavaScriptExecutableConstraint"));
        verifyExecutableRule(constraintBucket.getAll());
    }

    private void verifyExecutableRule(Collection<? extends AbstractRule> rules) {
        for (AbstractRule rule : rules) {
            assertThat(rule, instanceOf(ExecutableRule.class));
            assertThat(((ExecutableRule<?>) rule).getExecutable().getLanguage(), equalTo("javascript"));
        }
    }

    @Test
    public void ruleParameters() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/parameters.xml");
        Concept concept = ruleSet.getConceptBucket().getById("test:ConceptWithParameters");
        verifyParameters(concept, false);
        Concept conceptWithDefaultValues = ruleSet.getConceptBucket().getById("test:ConceptWithParametersAndDefaultValues");
        verifyParameters(conceptWithDefaultValues, true);
        Constraint constraint = ruleSet.getConstraintBucket().getById("test:ConstraintWithParameters");
        verifyParameters(constraint, false);
        Constraint constraintWithDefaultValues = ruleSet.getConstraintBucket().getById("test:ConstraintWithParametersAndDefaultValues");
        verifyParameters(constraintWithDefaultValues, true);
    }

    private void verifyParameters(ExecutableRule rule, boolean assertDefaultValue) {
        Map<String, Parameter> parameters = rule.getParameters();
        RuleSetTestHelper.verifyParameter(parameters, "charParam", Parameter.Type.CHAR, assertDefaultValue ? '4' : null);
        RuleSetTestHelper.verifyParameter(parameters, "byteParam", Parameter.Type.BYTE, assertDefaultValue ? (byte) 42 : null);
        RuleSetTestHelper.verifyParameter(parameters, "shortParam", Parameter.Type.SHORT, assertDefaultValue ? (short) 42 : null);
        RuleSetTestHelper.verifyParameter(parameters, "intParam", Parameter.Type.INT, assertDefaultValue ? 42 : null);
        RuleSetTestHelper.verifyParameter(parameters, "longParam", Parameter.Type.LONG, assertDefaultValue ? 42L : null);
        RuleSetTestHelper.verifyParameter(parameters, "floatParam", Parameter.Type.FLOAT, assertDefaultValue ? (float) 42.0 : null);
        RuleSetTestHelper.verifyParameter(parameters, "doubleParam", Parameter.Type.DOUBLE, assertDefaultValue ? 42.0 : null);
        RuleSetTestHelper.verifyParameter(parameters, "booleanParam", Parameter.Type.BOOLEAN, assertDefaultValue ? true : null);
        RuleSetTestHelper.verifyParameter(parameters, "stringParam", Parameter.Type.STRING, assertDefaultValue ? "FortyTwo" : null);
    }

}
