package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.TestDescriptor;

/**
 * Denotes a "test" artifact produced created by a Maven module (e.g. from
 * sources located in src/test/java".
 */
public interface MavenTestArtifactDescriptor extends MavenArtifactFileDescriptor, TestDescriptor {
}
