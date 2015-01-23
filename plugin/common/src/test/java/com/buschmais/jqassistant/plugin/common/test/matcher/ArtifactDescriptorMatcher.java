package com.buschmais.jqassistant.plugin.common.test.matcher;

import org.hamcrest.Matcher;

import com.buschmais.jqassistant.core.store.test.matcher.AbstractDescriptorMatcher;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;

/**
 * A matcher for {@link ArtifactFileDescriptor}s.
 */
public class ArtifactDescriptorMatcher extends AbstractDescriptorMatcher<ArtifactFileDescriptor> {

    /**
     * Constructor.
     * 
     * @param id
     *            The expected artifact id.
     */
    protected ArtifactDescriptorMatcher(String id) {
        super(ArtifactFileDescriptor.class, id);
    }

    /**
     * Return a {@link ArtifactDescriptorMatcher}.
     * 
     * @param id
     *            The artifact id.
     * @return The {@link ArtifactDescriptorMatcher}.
     */
    public static Matcher<? super ArtifactFileDescriptor> artifactDescriptor(String id) {
        return new ArtifactDescriptorMatcher(id);
    }
}
