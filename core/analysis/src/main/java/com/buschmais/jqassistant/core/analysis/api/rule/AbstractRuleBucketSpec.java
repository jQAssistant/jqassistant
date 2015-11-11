package com.buschmais.jqassistant.core.analysis.api.rule;

public interface AbstractRuleBucketSpec {

    void callingNewNoRuleExceptionThrowsCorrectException() throws Exception;

    void callingNewDuplicateRuleExceptionThrowsCorrectException() throws Exception;
}
