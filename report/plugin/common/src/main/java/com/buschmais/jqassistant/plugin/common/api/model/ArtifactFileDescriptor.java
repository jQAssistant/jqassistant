package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.plugin.common.api.report.Generic;

/**
 * Describes an artifact as a file.
 *
 * @author Herklotz
 */
@Generic(Generic.GenericLanguageElement.ArtifactFile)
public interface ArtifactFileDescriptor extends ArtifactDescriptor, FileDescriptor {
}
