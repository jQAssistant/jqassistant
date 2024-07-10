package com.buschmais.jqassistant.plugin.common.test.assertj;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.assertj.core.api.Condition;

/**
 * A {@link Condition} for asserting a {@link FileDescriptor} by its name.
 */
public class ArtifactDescriptorCondition extends Condition<ArtifactDescriptor> {

    private final String expectedFullyQualifiedName;

    private ArtifactDescriptorCondition(String expectedFullyQualifiedName) {
        super("artifact '" + expectedFullyQualifiedName + "'");
        this.expectedFullyQualifiedName = expectedFullyQualifiedName;
    }

    @Override
    public boolean matches(ArtifactDescriptor value) {
        return value.getFullQualifiedName()
            .equals(expectedFullyQualifiedName);
    }

    public static ArtifactDescriptorCondition artifactDescriptor(String expectedFullyQualifiedName) {
        return new ArtifactDescriptorCondition(expectedFullyQualifiedName);
    }
}
