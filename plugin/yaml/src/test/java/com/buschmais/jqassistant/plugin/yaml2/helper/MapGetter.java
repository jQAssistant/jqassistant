package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

import static java.lang.String.format;

public class MapGetter {

    private final YMLSequenceDescriptor ymlSequenceDescriptor;

    public MapGetter(YMLSequenceDescriptor descriptor) {
        ymlSequenceDescriptor = descriptor;
    }

    public YMLMapDescriptor getMap(int index) {
        Optional<YMLMapDescriptor> result = ymlSequenceDescriptor.getMaps().stream()
                                                                .filter(m -> m.getIndex() == 1)
                                                                .findFirst();

        String errorMessage = format("No map at index <%s> found", index);

        return result.orElseThrow(() -> new NoSuchElementException(errorMessage));
    }
}
