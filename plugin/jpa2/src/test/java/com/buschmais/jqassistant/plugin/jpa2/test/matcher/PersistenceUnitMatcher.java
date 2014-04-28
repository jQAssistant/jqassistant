package com.buschmais.jqassistant.plugin.jpa2.test.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor.PersistenceUnitDescriptor;

/**
 * A matcher for {@link PersistenceUnitDescriptor}s.
 */
public class PersistenceUnitMatcher extends TypeSafeMatcher<PersistenceUnitDescriptor> {

    private String name;

    public PersistenceUnitMatcher(String name) {
        this.name = name;
    }

    @Override
    protected boolean matchesSafely(PersistenceUnitDescriptor item) {
        return name.equals(item.getName());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a model unit with name ");
        description.appendValue(name);
    }

    /**
     * Return a {@link PersistenceUnitMatcher}.
     * 
     * @param name
     *            The expected name of the model unit.
     * @return The {@link PersistenceUnitMatcher}.
     */
    public static Matcher<? super PersistenceUnitDescriptor> persistenceUnitDescriptor(String name) {
        return new PersistenceUnitMatcher(name);
    }

}
