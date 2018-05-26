package com.buschmais.jqassistant.core.analysis.api;

import java.util.Map;

/**
 * Abstract base class for {@link RuleInterpreterPlugin}s.
 */
public abstract class AbstractRuleInterpreterPlugin implements RuleInterpreterPlugin {

    @Override
    public void initialize() {
    }

    @Override
    public void configure(Map<String, Object> properties) {
    }

}
