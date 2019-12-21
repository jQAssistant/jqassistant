package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.Optional;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLKeyDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;

import org.assertj.core.api.AbstractObjectAssert;

public class YMLMapAssert extends AbstractObjectAssert<YMLMapAssert, YMLMapDescriptor> {

    public YMLMapAssert(YMLMapDescriptor descriptor) {
        super(descriptor, YMLMapAssert.class);
    }

    public YMLMapAssert containsSimpleKeyWithName(String keyName) {
        isNotNull();

        String assertjErrorMessage = "\nExpecting map descriptor to contain key with name <%s>\nbut it doesn't contain such a key\n";

        Optional<YMLKeyDescriptor> result = actual.getKeys().stream().filter(k -> k.getName().equals(keyName)).findFirst();

        if (!result.isPresent()) {
            failWithMessage(assertjErrorMessage, keyName);
        }

        return this;
    }
}
