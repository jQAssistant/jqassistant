package com.buschmais.jqassistant.core.store.api.descriptor;

import java.io.File;
import java.util.Collection;

/**
 * Base interface for all project descriptors.
 */
public interface ProjectDescriptor extends Descriptor {

	public Collection<? extends File> getAdditionalFiles();
}
