package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;

import static java.lang.String.format;

public class MapGetter {

    private final Supplier<List<YMLMapDescriptor>> mapSupplier;

    public MapGetter(Supplier<List<YMLMapDescriptor>> supplier) {
        mapSupplier = supplier;
    }

    public YMLMapDescriptor getMap(int index) {
        return mapSupplier.get().get(index);
    }

    public YMLMapDescriptor getMapFromSequence(int index) {
        Optional<YMLMapDescriptor> result = mapSupplier.get().stream()
                                                       .filter(m -> m.getIndex() == index)
                                                       .findFirst();

        String errorMessage = format("No map at index <%s> found", index);

        return result.orElseThrow(() -> new NoSuchElementException(errorMessage));
    }
}
