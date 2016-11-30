package com.buschmais.jqassistant.core.analysis.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.rule.*;

public class XmlRuleSetReaderTest {

    @Test
    public void readScriptRule() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/javascript-rules.xml");
        assertThat(ruleSet.getConceptBucket().size(), equalTo(1));
        assertThat(ruleSet.getConstraintBucket().size(), equalTo(1));
        assertThat(ruleSet.getConceptBucket().getIds(), contains("test:JavaScriptConcept"));
        assertThat(ruleSet.getConstraintBucket().getIds(), contains("test:JavaScriptConstraint"));
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
        verifyParameter(parameters, "shortParam", Parameter.Type.SHORT, assertDefaultValue ? (short) 42 : null);
        verifyParameter(parameters, "intParam", Parameter.Type.INT, assertDefaultValue ? 42 : null);
        verifyParameter(parameters, "longParam", Parameter.Type.LONG, assertDefaultValue ? 42l : null);
        verifyParameter(parameters, "floatParam", Parameter.Type.FLOAT, assertDefaultValue ? (float) 42.0 : null);
        verifyParameter(parameters, "doubleParam", Parameter.Type.DOUBLE, assertDefaultValue ? 42.0 : null);
        verifyParameter(parameters, "booleanParam", Parameter.Type.BOOLEAN, assertDefaultValue ? true : null);
        verifyParameter(parameters, "stringParam", Parameter.Type.STRING, assertDefaultValue ? "FortyTwo" : null);
    }

    private <T> void verifyParameter(Map<String, Parameter> parameters, String parameter, Parameter.Type type, T defaultValue) {
        Parameter floatParameter = parameters.get(parameter);
        assertThat(floatParameter, notNullValue());
        assertThat(floatParameter.getName(), equalTo(parameter));
        assertThat(floatParameter.getType(), equalTo(type));
        assertThat(floatParameter.getDefaultValue(), Matchers.<Object> equalTo(defaultValue));
    }
}
