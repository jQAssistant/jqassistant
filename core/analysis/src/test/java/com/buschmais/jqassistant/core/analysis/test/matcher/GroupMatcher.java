package com.buschmais.jqassistant.core.analysis.test.matcher;

import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import org.hamcrest.Matcher;

/**
 * A matcher for {@link Group}s.
 */
public class GroupMatcher extends AbstractRuleMatcher<Group> {

    /**
     * Constructor.
     *
     * @param id The expected group id.
     */
    protected GroupMatcher(String id) {
        super(Group.class, id);
    }

    /**
     * Return a {@link GroupMatcher}.
     *
     * @param id The group id.
     * @return The {@link GroupMatcher}.
     */
    public static Matcher<? super Group> group(String id) {
        return new GroupMatcher(id);
    }
}
