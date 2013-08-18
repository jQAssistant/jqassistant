package com.buschmais.jqassistant.core.model.test.matcher.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import org.hamcrest.Matcher;

/**
 * A matcher for {@link PackageDescriptor}s.
 */
public class PackageDescriptorMatcher extends AbstractDescriptorMatcher<PackageDescriptor> {

    /**
     * Constructor.
     *
     * @param p The expected package.
     */
    protected PackageDescriptorMatcher(Package p) {
        super(PackageDescriptor.class, p.getName());
    }

    /**
     * Constructor.
     *
     * @param name The expected full qualified package name.
     */
    protected PackageDescriptorMatcher(String name) {
        super(PackageDescriptor.class, name);
    }

    /**
     * Return a {@link PackageDescriptorMatcher} .
     *
     * @param p The expected package.
     * @return The {@link PackageDescriptorMatcher}.
     */
    public static Matcher<? super PackageDescriptor> packageDescriptor(Package p) {
        return new PackageDescriptorMatcher(p);
    }

    /**
     * Return a {@link PackageDescriptorMatcher}.
     *
     * @param name The expected full qualified package name.
     * @return The {@link PackageDescriptorMatcher}.
     */
    public static Matcher<? super PackageDescriptor> packageDescriptor(String name) {
        return new PackageDescriptorMatcher(name);
    }
}
