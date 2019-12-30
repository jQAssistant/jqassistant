package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.Objects;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;

import org.assertj.core.api.AbstractObjectAssert;

public class YMLScalarAssert extends AbstractObjectAssert<YMLScalarAssert, YMLScalarDescriptor> {
    public YMLScalarAssert(YMLScalarDescriptor descriptor) {
        super(descriptor, YMLScalarAssert.class);
    }

    public YMLScalarAssert hasValue(String expectedValue) {
        isNotNull();

        String assertjErrorMessage = "\nExpecting scalar descriptor to have value of <%s>\n" +
                                     "but its actual value is <%s>\n";

        String actualValue = actual.getValue();
        if (!Objects.equals(expectedValue, actualValue)) {
            failWithMessage(assertjErrorMessage, expectedValue, actualValue);
        }

        return this;
    }

}
