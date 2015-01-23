package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;

/**
 * Describes an artifact as a file.
 * 
 * @author Herklotz
 */
@Generic(Generic.GenericLanguageElement.ArtifactFile)
public interface ArtifactFileDescriptor extends ArtifactDescriptor, FileDescriptor, FileContainerDescriptor {

}
