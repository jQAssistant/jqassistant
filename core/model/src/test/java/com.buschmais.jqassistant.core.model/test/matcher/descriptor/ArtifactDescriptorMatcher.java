package com.buschmais.jqassistant.core.model.test.matcher.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import org.hamcrest.Matcher;

/**
 * A matcher for {@link PackageDescriptor}s.
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
