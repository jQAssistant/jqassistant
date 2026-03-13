package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.report.Generic;

/**
 * Defines a directory containing java classes.
 */
@Generic(Generic.GenericLanguageElement.ArtifactFile)
public interface JavaClassesDirectoryDescriptor extends JavaArtifactFileDescriptor, DirectoryDescriptor {
}
