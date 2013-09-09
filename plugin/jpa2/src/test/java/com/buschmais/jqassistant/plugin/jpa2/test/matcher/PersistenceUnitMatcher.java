package com.buschmais.jqassistant.plugin.jpa2.test.matcher;

import com.buschmais.jqassistant.core.store.test.matcher.AbstractDescriptorMatcher;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor.PersistenceUnitDescriptor;
import org.hamcrest.Matcher;

/**
 * A matcher for {@link PersistenceUnitDescriptor}s.
 */
public class PersistenceUnitMatcher extends AbstractDescriptorMatcher<PersistenceUnitDescriptor> {

    /**
     * Constructor.
     *
     * @param name The expected full qualified types name.
     */
    protected PersistenceUnitMatcher(String name) {
        super(PersistenceUnitDescriptor.class, name);
    }

    /**
     * Return a {@link PersistenceUnitMatcher}.
     *
     * @param name The expected name of the persistence unit.
     * @return The {@link PersistenceUnitMatcher}.
     */
    public static Matcher<? super PersistenceUnitDescriptor> persistenceUnitDescriptor(String name) {
        return new PersistenceUnitMatcher(name);
    }
}
