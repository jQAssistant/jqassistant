package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLKeyDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;

import static java.lang.String.format;

public class KeyGetter {

    private final YMLMapDescriptor ymlMapDescriptor;

    public KeyGetter(YMLMapDescriptor descriptor) {
        ymlMapDescriptor = descriptor;
    }

    public YMLKeyDescriptor getKeyByName(String keyName) {
        Optional<YMLKeyDescriptor> result = ymlMapDescriptor.getKeys().stream()
                                                            .filter(key -> key.getName().equals(keyName))
                                                            .findFirst();
        String errorMessage = format("No key with name <%s> found", keyName);

        return result.orElseThrow(() -> new NoSuchElementException(errorMessage));
    }
}
