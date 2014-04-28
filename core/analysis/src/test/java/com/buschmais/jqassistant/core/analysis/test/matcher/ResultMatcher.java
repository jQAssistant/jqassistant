package com.buschmais.jqassistant.core.analysis.test.matcher;

import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;

/**
 * A matcher for {@link Result}s.
 */
public class ResultMatcher<E extends AbstractExecutable> extends TypeSafeMatcher<Result<E>> {

    private Matcher<? extends AbstractExecutable> executableMatcher;
    private Matcher<? super Iterable<? super Map<?, ?>>> rowsMatcher;

    /**
     * Constructor.
     * 
     * @param executableMatcher
     *            The expected executable type.
     */
    protected ResultMatcher(Matcher<? extends AbstractExecutable> executableMatcher, Matcher<? super Iterable<? super Map<?, ?>>> rowsMatcher) {
        this.executableMatcher = executableMatcher;
        this.rowsMatcher = rowsMatcher;
    }

    /**
     * Constructor.
     * 
     * @param executableMatcher
     *            The expected executable type.
     */
    protected ResultMatcher(Matcher<? extends AbstractExecutable> executableMatcher) {
        this(executableMatcher, null);
    }

    @Override
    public boolean matchesSafely(Result<E> item) {
        return (this.executableMatcher.matches(item.getExecutable()) && (rowsMatcher == null) || rowsMatcher.matches(item.getRows()));
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
        if (!executableMatcher.matches(item.getExecutable())) {
            executableMatcher.describeMismatch(item.getExecutable(), mismatchDescription);
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
    public static <E extends AbstractExecutable> Matcher<? super Result<E>> result(Matcher<E> constraintMatcher) {
        return new ResultMatcher<E>(constraintMatcher);
    }

    /**
     * Return a {@link ResultMatcher}.
     * 
     * @param constraintMatcher
     *            The matcher for the expected constraint.
     * @return The {@link ResultMatcher}.
     */
    public static <E extends AbstractExecutable> Matcher<? super Result<E>> result(Matcher<E> constraintMatcher,
            Matcher<? super Iterable<? super Map<?, ?>>> rowsMatcher) {
        return new ResultMatcher<E>(constraintMatcher, rowsMatcher);
    }
}
