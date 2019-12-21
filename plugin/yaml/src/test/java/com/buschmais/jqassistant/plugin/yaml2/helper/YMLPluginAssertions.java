package com.buschmais.jqassistant.plugin.yaml2.helper;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.assertj.core.api.Assertions;

public class YMLPluginAssertions extends Assertions {

    public static YMLKeyAssert assertThat(YMLKeyDescriptor descriptor) {
        return new YMLKeyAssert(descriptor);
    }

    public static YMLFileAssert assertThat(YMLFileDescriptor descriptor) {
        return new YMLFileAssert(descriptor);
    }

    public static YMLDocumentAssert assertThat(YMLDocumentDescriptor descriptor) {
        return new YMLDocumentAssert(descriptor);
    }

    public static YMLSequenceAssert assertThat(YMLSequenceDescriptor descriptor) {
        return new YMLSequenceAssert(descriptor);
    }

    public static YMLMapAssert assertThat(YMLMapDescriptor descriptor) {
        return new YMLMapAssert(descriptor);
    }

    public static YMLScalarAssert assertThat(YMLScalarDescriptor descriptor) {
        return new YMLScalarAssert(descriptor);
    }

    protected YMLPluginAssertions() {
    }
}
