package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.Generic;

/**
 * Defines a directory containing java classes.
 */
@Generic(Generic.GenericLanguageElement.ArtifactFile)
public interface JavaClassesDirectoryDescriptor extends JavaArtifactDescriptor, DirectoryDescriptor {
}
