package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

import static java.lang.String.format;

public class ScalarGetter {

    private final YMLSequenceDescriptor ymlSequenceDescriptor;

    public ScalarGetter(YMLSequenceDescriptor descriptor) {
        ymlSequenceDescriptor = descriptor;
    }

    public YMLScalarDescriptor getScalar(int index) {
        Optional<YMLScalarDescriptor> result =
            ymlSequenceDescriptor.getScalars().stream()
                                 .filter(sd -> sd.getIndex() == index)
                                 .findFirst();

        String errorMessage = format("No scalar at index <%s> found", index);

        return result.orElseThrow(() -> new NoSuchElementException(errorMessage));
    }
}
