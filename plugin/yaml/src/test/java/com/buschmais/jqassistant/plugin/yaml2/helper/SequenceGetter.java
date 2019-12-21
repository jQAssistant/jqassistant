package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.List;
import java.util.function.Supplier;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

public class SequenceGetter {
    private final Supplier<List<YMLSequenceDescriptor>> seqSupplier;


    public SequenceGetter(Supplier<List<YMLSequenceDescriptor>> supplier) {
        seqSupplier = supplier;

    }

    public YMLSequenceDescriptor getSequence(int index) {
        /* Implementation notice
         * Actually there is no reliable order in a YML document,
         * except for sequences or collections as it is called
         * in the YAML specification. I think this is a common misunderstanding.
         * Therefore all this get...(int) methods rely on the implementation.
         * Oliver B. Fischer // 2019-12-20
         */
        return seqSupplier.get().get(index);
    }
}
