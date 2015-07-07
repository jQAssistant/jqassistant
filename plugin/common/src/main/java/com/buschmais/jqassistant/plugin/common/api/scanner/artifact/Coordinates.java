package com.buschmais.jqassistant.plugin.common.api.scanner.artifact;

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
