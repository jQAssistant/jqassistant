package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a path, e.g. in a file system.
 */
@Abstract
@Label("Path")
public interface PathDescriptor {

    /**
     * Return the path relative to the project root.
     * May include archives, paths are concatenated, e.g. "files/assets.zip/styles.css"
     *
     * @return The path.
     */
    String getPath();

    void setPath(String path);

}
