package com.buschmais.jqassistant.core.analysis.api.rule;

import org.junit.Test;

public class TemplateBucketTest implements AbstractRuleBucketSpec {

    @Override
    @Test(expected = NoTemplateException.class)
    public void callingNewNoRuleExceptionThrowsCorrectException() throws NoTemplateException {
        throw new TemplateBucket().newNoRuleException("x");
    }

    @Override
    @Test(expected = DuplicateTemplateException.class)
    public void callingNewDuplicateRuleExceptionThrowsCorrectException() throws DuplicateTemplateException {
        throw new TemplateBucket().newDuplicateRuleException("x");
    }
}