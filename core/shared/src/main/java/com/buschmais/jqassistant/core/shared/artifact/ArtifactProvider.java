package com.buschmais.jqassistant.core.shared.artifact;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.core.shared.aether.configuration.Plugin;

public interface ArtifactProvider {

    List<File> resolve(List<Plugin> plugins);

}
