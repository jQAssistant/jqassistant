package com.buschmais.jqassistant.core.rule.api.model;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlRuleParserPlugin;

import org.hamcrest.core.IsCollectionContaining;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class XmlRuleParserPluginTest {

    @Mock
    private Rule configuration;

    @Test
    void scriptRule() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/javascript-rules.xml", configuration);
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
    void ruleParameters() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/parameters.xml", configuration);
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

    @Test
    void urlSource() throws Exception {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        URL url = getClass().getResource("/test-concepts.xml");
        RuleParserPlugin reader = new XmlRuleParserPlugin();
        reader.initialize();
        reader.configure(configuration);
        UrlRuleSource ruleSource = new UrlRuleSource(url);
        assertThat(reader.accepts(ruleSource), equalTo(true));
        reader.parse(ruleSource, ruleSetBuilder);
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
        assertThat(ruleSet.getConceptBucket().size(), equalTo(1));
        assertThat(ruleSet.getConstraintBucket().size(), equalTo(1));
        assertThat(ruleSet.getConceptBucket().getIds(), contains("java:Throwable"));
        assertThat(ruleSet.getConstraintBucket().getIds(), contains("example:ConstructorOfDateMustNotBeUsed"));
        assertThat(ruleSet.getGroupsBucket().size(), equalTo(1));

        Group group = ruleSet.getGroupsBucket().getById("default");
        assertThat(group.getId(), equalTo("default"));
    }

    @Test
    void ruleSchema_1_8() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/rules-1.8.xml", configuration);
        Set<String> conceptIds = ruleSet.getConceptBucket().getIds();
        assertThat(conceptIds.size(), equalTo(1));
        assertThat(conceptIds, IsCollectionContaining.hasItems("test"));
    }
}
