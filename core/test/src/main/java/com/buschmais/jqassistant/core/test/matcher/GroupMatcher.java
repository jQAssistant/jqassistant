package com.buschmais.jqassistant.core.test.matcher;

import com.buschmais.jqassistant.core.rule.api.model.Group;

/**
 * A matcher for {@link Group}s.
 */
public class GroupMatcher extends com.buschmais.jqassistant.core.analysis.test.matcher.GroupMatcher {

    /**
     * Constructor.
     *
     * @param id
     *     The expected group id.
     */
    protected GroupMatcher(String id) {
        super(id);
    }
}
