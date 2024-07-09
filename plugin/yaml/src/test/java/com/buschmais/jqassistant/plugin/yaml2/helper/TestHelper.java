package com.buschmais.jqassistant.plugin.yaml2.helper;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

public class TestHelper {

    public static DocumentGetter getDocuments(YMLFileDescriptor descriptor) {
        return new DocumentGetter(descriptor);
    }

    public static KeyGetter getKeys(YMLMapDescriptor descriptor) {
        return new KeyGetter(descriptor);
    }

    public static SequenceGetter getSequences(YMLDocumentDescriptor descriptor) {
        return new SequenceGetter(descriptor::getSequences);
    }

    public static SequenceGetter getSequences(YMLSequenceDescriptor descriptor) {
        return new SequenceGetter(descriptor::getSequences);
    }

    public static MapGetter getMaps(YMLSequenceDescriptor descriptor) {
        return new MapGetter(descriptor::getMaps);
    }

    public static MapGetter getMaps(YMLDocumentDescriptor descriptor) {
        return new MapGetter(descriptor::getMaps);
    }

    public static ScalarGetter getScalars(YMLSequenceDescriptor descriptor) {
        return new ScalarGetter(descriptor::getScalars);
    }

    public static ScalarGetter getScalars(YMLDocumentDescriptor descriptor) {
        return new ScalarGetter(descriptor::getScalars);
    }
}
