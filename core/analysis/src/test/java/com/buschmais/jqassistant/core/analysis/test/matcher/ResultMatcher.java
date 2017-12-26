package com.buschmais.jqassistant.core.analysis.test.matcher;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher for {@link Result}s.
 */
public class ResultMatcher<E extends AbstractExecutableRule> extends TypeSafeMatcher<Result<E>> {

    private Matcher<? extends ExecutableRule> executableMatcher;
    private Matcher<? super Iterable<? super Map<?, ?>>> rowsMatcher;

    /**
     * Constructor.
     * 
     * @param executableMatcher
     *            The expected executable type.
     */
    protected ResultMatcher(Matcher<? extends ExecutableRule> executableMatcher, Matcher<? super Iterable<? super Map<?, ?>>> rowsMatcher) {
        this.executableMatcher = executableMatcher;
        this.rowsMatcher = rowsMatcher;
    }

    /**
     * Constructor.
     * 
     * @param executableMatcher
     *            The expected executable type.
     */
    protected ResultMatcher(Matcher<? extends ExecutableRule> executableMatcher) {
        this(executableMatcher, null);
    }

    @Override
    public boolean matchesSafely(Result<E> item) {
        return (this.executableMatcher.matches(item.getRule()) && (rowsMatcher == null) || rowsMatcher.matches(item.getRows()));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Result(");
        executableMatcher.describeTo(description);
        if (rowsMatcher != null) {
            description.appendText("(rows=");
            rowsMatcher.describeTo(description);
            description.appendText(")");
        }
        description.appendText(")");
    }

    @Override
    protected void describeMismatchSafely(Result<E> item, Description mismatchDescription) {
        mismatchDescription.appendText("Result(");
        if (!executableMatcher.matches(item.getRule())) {
            executableMatcher.describeMismatch(item.getRule(), mismatchDescription);
        }
        if (rowsMatcher != null && !rowsMatcher.matches(item.getRows())) {
            rowsMatcher.describeMismatch(item.getRows(), mismatchDescription);
        }
        mismatchDescription.appendText(")");
    }

    /**
     * Return a {@link ResultMatcher}.
     * 
     * @param constraintMatcher
     *            The matcher for the expected constraint.
     * @return The {@link ResultMatcher}.
     */
    public static <E extends AbstractExecutableRule> Matcher<? super Result<E>> result(Matcher<E> constraintMatcher) {
        return new ResultMatcher<E>(constraintMatcher);
    }

    /**
     * Return a {@link ResultMatcher}.
     * 
     * @param constraintMatcher
     *            The matcher for the expected constraint.
     * @return The {@link ResultMatcher}.
     */
    public static <E extends AbstractExecutableRule> Matcher<? super Result<E>> result(Matcher<E> constraintMatcher,
            Matcher<? super Iterable<? super Map<?, ?>>> rowsMatcher) {
        return new ResultMatcher<E>(constraintMatcher, rowsMatcher);
    }
}
