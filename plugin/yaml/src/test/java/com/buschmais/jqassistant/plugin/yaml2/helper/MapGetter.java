package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.List;
import java.util.function.Supplier;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;

public class MapGetter {

    private final Supplier<List<YMLMapDescriptor>> mapSupplier;

    public MapGetter(Supplier<List<YMLMapDescriptor>> supplier) {
        mapSupplier = supplier;
    }

    public YMLMapDescriptor getMapByParsePosition(int index) {
        return mapSupplier.get().get(index);
    }

}
