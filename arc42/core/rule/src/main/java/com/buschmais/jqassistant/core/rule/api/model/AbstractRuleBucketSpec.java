package com.buschmais.jqassistant.core.rule.api.model;

public interface AbstractRuleBucketSpec {

    void callingNewNoRuleExceptionThrowsCorrectException() throws Exception;

    void callingNewDuplicateRuleExceptionThrowsCorrectException() throws Exception;
}
