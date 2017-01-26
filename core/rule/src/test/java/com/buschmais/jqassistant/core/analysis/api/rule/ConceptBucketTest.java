package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.api.rule.AbstractRuleBucketSpec;
import com.buschmais.jqassistant.core.analysis.api.rule.ConceptBucket;
import com.buschmais.jqassistant.core.analysis.api.rule.DuplicateConceptException;
import com.buschmais.jqassistant.core.analysis.api.rule.NoConceptException;
import org.junit.Test;

public class ConceptBucketTest implements AbstractRuleBucketSpec {

    @Override
    @Test(expected = NoConceptException.class)
    public void callingNewNoRuleExceptionThrowsCorrectException() throws Exception {
        throw new ConceptBucket().newNoRuleException("x");
    }

    @Override
    @Test(expected = DuplicateConceptException.class)
    public void callingNewDuplicateRuleExceptionThrowsCorrectException() throws Exception {
        throw new ConceptBucket().newDuplicateRuleException("x");
    }
}