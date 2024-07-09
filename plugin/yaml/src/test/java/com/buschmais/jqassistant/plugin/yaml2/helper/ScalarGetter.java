package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;

import static java.lang.String.format;

public class ScalarGetter {

    private final Supplier<List<YMLScalarDescriptor>> supplier;

    public ScalarGetter(Supplier<List<YMLScalarDescriptor>> supplier) {
        this.supplier = supplier;
    }

    public YMLScalarDescriptor getScalarBySeqIndex(int index) {
        Optional<YMLScalarDescriptor> result =
            supplier.get().stream()
                    .filter(sd -> sd.getIndex() == index)
                    .findFirst();

        String errorMessage = format("No scalar at index <%s> found", index);

        return result.orElseThrow(() -> new NoSuchElementException(errorMessage));
    }

    public YMLScalarDescriptor getScalarByParsePosition(int index) {
        return supplier.get().get(index);
    }
}
