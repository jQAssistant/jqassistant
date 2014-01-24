package com.buschmais.jqassistant.core.store.api.descriptor;

import java.io.File;
import java.util.Collection;

public interface ProjectDescriptor extends Descriptor {

	public Collection<? extends File> getAdditionalFiles();
}
