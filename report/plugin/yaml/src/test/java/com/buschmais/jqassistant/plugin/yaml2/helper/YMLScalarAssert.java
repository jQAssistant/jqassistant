package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.Objects;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;

public class YMLScalarAssert extends AbstractYMLAssert<YMLScalarAssert, YMLScalarDescriptor> {
    public YMLScalarAssert(YMLScalarDescriptor descriptor) {
        super(descriptor, YMLScalarAssert.class);
    }

    @Override
    public YMLScalarAssert andContinueAssertionOnThis() {
        return this;
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

    public YMLScalarAssert withSequenceIndex(int expectedValue) {
        isNotNull();

        String assertjErrorMessage = "\nExpecting scalar descriptor to have an " +
                                     "index of <%s>\n" +
                                     "but its actual index is <%s>\n";

        Integer actualValue = actual.getIndex();
        if (!Objects.equals(expectedValue, actualValue)) {
            failWithMessage(assertjErrorMessage, expectedValue, actualValue);
        }

        return this;
    }

    public YMLScalarAssert hasEmptyValue() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting scalar descriptor to have value of <>\n" +
                                     "but its actual value is <%s>\n";

        String actualValue = actual.getValue();
        if (!"".equals(actualValue)) {
            failWithMessage(assertjErrorMessage, actualValue);
        }

        return this;
    }
}
