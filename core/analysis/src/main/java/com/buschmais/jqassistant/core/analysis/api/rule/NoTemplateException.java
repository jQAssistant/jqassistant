package com.buschmais.jqassistant.core.analysis.api.rule;

public class NoTemplateException extends NoRuleException {
    public NoTemplateException(String conceptId) {
        super(conceptId);
    }
}
