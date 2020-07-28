package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

// See https://github.com/jQAssistant/jqa-core-framework/issues/53
public class GraphGenerationFailedException extends RuntimeException {
    public GraphGenerationFailedException(String message) {
        super(message);
    }
}
