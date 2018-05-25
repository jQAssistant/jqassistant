package com.buschmais.jqassistant.core.analysis.api;

import java.util.Map;

/**
 * Abstract base class for {@link RuleLanguagePlugin}s.
 */
public abstract class AbstractRuleLanguagePlugin implements RuleLanguagePlugin {

    @Override
    public void initialize() {
    }

    @Override
    public void configure(Map<String, Object> properties) {
    }

}
