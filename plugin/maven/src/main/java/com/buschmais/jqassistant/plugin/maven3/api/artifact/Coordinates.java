package com.buschmais.jqassistant.plugin.maven3.api.artifact;

/**
 * Defines unique artifact coordinates.
 */
public interface Coordinates {

    String getGroup();

    String getName();

    String getClassifier();

    String getType();

    String getVersion();

}
