package com.buschmais.jqassistant.core.rule.api.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstraintBucketTest implements AbstractRuleBucketSpec {
    @Override
    @Test
    public void callingNewNoRuleExceptionThrowsCorrectException() throws NoConstraintException {
        Assertions.assertThatThrownBy(() -> { throw new ConstraintBucket().newNoRuleException("x"); })
                  .isInstanceOf(NoConstraintException.class);
    }

    @Override
    @Test
    public void callingNewDuplicateRuleExceptionThrowsCorrectException() throws DuplicateConstraintException {
        Assertions.assertThatThrownBy(() -> { throw new ConstraintBucket().newDuplicateRuleException("x"); })
                  .isInstanceOf(DuplicateConstraintException.class);
    }
}
