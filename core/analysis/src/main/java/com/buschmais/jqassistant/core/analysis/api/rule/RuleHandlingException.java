package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.api.RuleException;

public class RuleHandlingException extends RuleException {
    public RuleHandlingException(String message) {
        super(message);
    }
}
