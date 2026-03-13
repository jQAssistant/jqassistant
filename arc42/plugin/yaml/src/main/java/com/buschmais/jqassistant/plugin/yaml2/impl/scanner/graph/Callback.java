package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.function.Consumer;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;

interface Callback<D extends YMLDescriptor> extends Consumer<D> {
    void created(D descriptor);

    @Override
    default void accept(D descriptor) {
        created(descriptor);
    }
}
