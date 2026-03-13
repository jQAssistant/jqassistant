package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.Optional;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSimpleKeyDescriptor;

public class YMLMapAssert extends AbstractYMLAssert<YMLMapAssert, YMLMapDescriptor> {

    public YMLMapAssert(YMLMapDescriptor descriptor) {
        super(descriptor, YMLMapAssert.class);
    }

    @Override
    public YMLMapAssert andContinueAssertionOnThis() {
        return this;
    }

    public YMLMapAssert containsSimpleKeyWithName(String keyName) {
        isNotNull();

        String assertjErrorMessage = "\nExpecting map descriptor to contain key with name <%s>\nbut it doesn't contain such a key\n";

        Optional<YMLSimpleKeyDescriptor> result = actual.getKeys().stream().filter(k -> k.getName().equals(keyName)).findFirst();

        if (!result.isPresent()) {
            failWithMessage(assertjErrorMessage, keyName);
        }

        return this;
    }

    public YMLMapAssert hasNoSimpleKeys() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting map descriptor not to have any simple key\nbut it has <%s> simple keys\n";

        boolean empty = actual.getKeys().isEmpty();
        if (!empty) {
            int size = actual.getKeys().size();
            failWithMessage(assertjErrorMessage, size);
        }

        return this;
    }

    public YMLMapAssert hasComplexKeys(int expectedCount) {
        isNotNull();

        String assertjErrorMessage = "\nExpecting map to have <%s> complex keys\n" +
                                     "but the map has <%s> complex keys";

        int size = actual.getComplexKeys().size();

        if (size != expectedCount) {
            failWithMessage(assertjErrorMessage, expectedCount, size);
        }

        return this;
    }
}
