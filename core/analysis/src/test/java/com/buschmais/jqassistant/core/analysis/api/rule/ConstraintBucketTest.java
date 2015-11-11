package com.buschmais.jqassistant.core.analysis.api.rule;

import org.junit.Test;

public class ConstraintBucketTest implements AbstractRuleBucketSpec {
    @Override
    @Test(expected = NoConstraintException.class)
    public void callingNewNoRuleExceptionThrowsCorrectException() throws NoConstraintException {
        throw new ConstraintBucket().newNoRuleException("x");
    }

    @Override
    @Test(expected = DuplicateConstraintException.class)
    public void callingNewDuplicateRuleExceptionThrowsCorrectException() throws DuplicateConstraintException {
        throw new ConstraintBucket().newDuplicateRuleException("x");
    }
}
