package com.buschmais.jqassistant.core.test.matcher;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.AbstractExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import org.hamcrest.Matcher;

/**
 * A matcher for {@link Result}s.
 */
public class ResultMatcher<E extends AbstractExecutableRule> extends com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher<E> {

    protected ResultMatcher(Matcher<? extends ExecutableRule> executableMatcher, Matcher<? super Iterable<? super Map<?, ?>>> rowsMatcher) {
        super(executableMatcher, rowsMatcher);
    }

    protected ResultMatcher(Matcher<? extends ExecutableRule> executableMatcher) {
        super(executableMatcher);
    }
}
