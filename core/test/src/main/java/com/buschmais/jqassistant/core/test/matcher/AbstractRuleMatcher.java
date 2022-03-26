package com.buschmais.jqassistant.core.test.matcher;

import com.buschmais.jqassistant.core.rule.api.model.Rule;

/**
 * Abstract base class for rules matchers.
 */
public class AbstractRuleMatcher<T extends Rule> extends com.buschmais.jqassistant.core.analysis.test.matcher.AbstractRuleMatcher<T> {

    /**
     * Constructor.
     *
     * @param type
     *     The rules type.
     * @param id
     */
    protected AbstractRuleMatcher(Class<T> type, String id) {
        super(type, id);
    }
}
