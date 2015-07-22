package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ApplicationDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;

/**
 * Describes a WAR archive.
 */
public interface WebApplicationArchiveDescriptor extends WebDescriptor, ApplicationDescriptor, ArchiveDescriptor, JavaArtifactFileDescriptor {
}
