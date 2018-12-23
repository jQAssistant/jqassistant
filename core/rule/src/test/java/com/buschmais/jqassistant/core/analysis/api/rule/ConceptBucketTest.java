package com.buschmais.jqassistant.core.analysis.api.rule;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConceptBucketTest implements AbstractRuleBucketSpec {

    @Override
    @Test
    public void callingNewNoRuleExceptionThrowsCorrectException() throws Exception {
        Assertions.assertThatThrownBy(()-> { throw new ConceptBucket().newNoRuleException("x"); })
                  .isInstanceOf(NoRuleException.class);
    }

    @Override
    @Test
    public void callingNewDuplicateRuleExceptionThrowsCorrectException() throws Exception {
        Assertions.assertThatThrownBy(() -> { throw new ConceptBucket().newDuplicateRuleException("x"); })
                  .isInstanceOf(DuplicateRuleException.class);
    }
}
