package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static com.buschmais.jqassistant.core.analysis.api.rule.RuleSetTestHelper.readRuleSet;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExecutablesTest {

    @Test
    public void xml() throws Exception {
        veritfyRuleset(readRuleSet("/executables.xml"));
    }

    @Test
    public void asciidoc() throws Exception {
        RuleSet ruleSet = readRuleSet("/executables.adoc");
        veritfyRuleset(ruleSet);
        verifyConceptExecutable(ruleSet, "test:Table", SourceExecutable.class);
    }

    private void veritfyRuleset(RuleSet ruleSet) throws NoConceptException, NoConstraintException {
        verifyConceptExecutable(ruleSet, "test:CypherConcept", CypherExecutable.class);
        verifyConceptExecutable(ruleSet, "test:ScriptConcept", ScriptExecutable.class);
        verifyConceptExecutable(ruleSet, "test:SourceConcept", SourceExecutable.class);
        verifyConceptExecutable(ruleSet, "test:SourceConceptUpperCase", SourceExecutable.class);
        verifyConstraintExecutable(ruleSet, "test:CypherConstraint", CypherExecutable.class);
        verifyConstraintExecutable(ruleSet, "test:ScriptConstraint", ScriptExecutable.class);
        verifyConstraintExecutable(ruleSet, "test:SourceConstraint", SourceExecutable.class);
        verifyConstraintExecutable(ruleSet, "test:SourceConstraintUpperCase", SourceExecutable.class);
    }

    private void verifyConceptExecutable(RuleSet ruleSet, String id, Class<? extends Executable> type) throws NoConceptException {
        Concept concept = ruleSet.getConceptBucket().getById(id);
        assertThat(concept, notNullValue());
        assertThat(concept.getId(), concept.getExecutable(), CoreMatchers.<Executable> instanceOf(type));
    }

    private void verifyConstraintExecutable(RuleSet ruleSet, String id, Class<? extends Executable> type) throws NoConstraintException {
        Constraint constraint = ruleSet.getConstraintBucket().getById(id);
        assertThat(constraint, notNullValue());
        assertThat(constraint.getExecutable(), CoreMatchers.<Executable> instanceOf(type));
    }

}
