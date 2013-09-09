package com.buschmais.jqassistant.plugin.common.test.matcher;

import com.buschmais.jqassistant.core.model.test.matcher.descriptor.AbstractDescriptorMatcher;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.ArtifactDescriptor;
import org.hamcrest.Matcher;

/**
 * A matcher for {@link ArtifactDescriptor}s.
 */
public class ArtifactDescriptorMatcher extends AbstractDescriptorMatcher<ArtifactDescriptor> {

    /**
     * Constructor.
     *
     * @param id The expected artifact id.
     */
    protected ArtifactDescriptorMatcher(String id) {
        super(ArtifactDescriptor.class, id);
    }

    /**
     * Return a {@link ArtifactDescriptorMatcher}.
     *
     * @param id The artifact id.
     * @return The {@link ArtifactDescriptorMatcher}.
     */
    public static Matcher<? super ArtifactDescriptor> artifactDescriptor(String id) {
        return new ArtifactDescriptorMatcher(id);
    }
}
