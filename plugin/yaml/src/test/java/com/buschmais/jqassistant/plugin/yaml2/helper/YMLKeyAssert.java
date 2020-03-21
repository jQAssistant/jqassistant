package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.Objects;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

public class YMLKeyAssert extends AbstractYMLAssert<YMLKeyAssert, YMLSimpleKeyDescriptor> {

    public YMLKeyAssert(YMLSimpleKeyDescriptor descriptor) {
        super(descriptor, YMLKeyAssert.class);
    }

    @Override
    public YMLKeyAssert andContinueAssertionOnThis() {
        return this;
    }

    public YMLKeyAssert hasName(String expectedName) {
        isNotNull();

        String assertjErrorMessage = "\nExpecting key descriptor to have <%s> as name\nbut has <%s> as name\n";

        String actualName = actual.getName();

        if (!actualName.equals(expectedName)) {
            failWithMessage(assertjErrorMessage, actualName, expectedName);
        }

        return this;
    }

    public YMLKeyAssert hasValue() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting key descriptor to have a value\n" +
                                     "but it hasn't a value\n";

        if (actual.getValue() == null) {
            failWithMessage(assertjErrorMessage);
        }

        return this;
    }

    public YMLKeyAssert hasScalarAsValue() {
        hasValue();

        String assertjErrorMessage = "\nExpecting key descriptor to have a <%s> as value\n" +
                                     "but it has a <%s> as value\n";

        YMLDescriptor value = actual.getValue();

        if (!(value instanceof YMLScalarDescriptor)) {
            String actualType = getActualTypeOf(value);

            failWithMessage(assertjErrorMessage, "scalar", actualType);
        }

        return this;
    }

    public YMLKeyAssert hasScalarValue(String expectedValue) {
        hasScalarAsValue();

        String assertjErrorMessage = "\nExpecting key descriptor to have scalar value of <%s>\n" +
                                     "but its actual value is <%s>\n";

        YMLScalarDescriptor scalarValue = YMLScalarDescriptor.class.cast(actual.getValue());
        String actualValue = scalarValue.getValue();
        if (!Objects.equals(expectedValue, actualValue)) {
            failWithMessage(assertjErrorMessage, expectedValue, actualValue);
        }

        return this;
    }

    public YMLKeyAssert hasMapAsValue() {
        hasValue();

        String assertjErrorMessage = "\nExpecting key descriptor to have a <%s> as value\n" +
                                     "but it has a <%s> as value\n";

        YMLDescriptor value = actual.getValue();

        if (!(value instanceof YMLMapDescriptor)) {
            String actualType = getActualTypeOf(value);

            failWithMessage(assertjErrorMessage, "map", actualType);
        }

        return this;
    }

    public YMLKeyAssert hasSequenceAsValue() {
        hasValue();

        String assertjErrorMessage = "\nExpecting key descriptor to have a <%s> as value\n" +
                                     "but it has a <%s> as value\n";

        YMLDescriptor value = actual.getValue();

        if (!(value instanceof YMLSequenceDescriptor)) {
            String actualType = getActualTypeOf(value);

            failWithMessage(assertjErrorMessage, "sequence", actualType);
        }

        return this;
    }

    private static String getActualTypeOf(YMLDescriptor descriptor) {
        String actualType = null;

        if (descriptor instanceof YMLMapDescriptor) {
            actualType = "map";
        } else if (descriptor instanceof YMLSequenceDescriptor) {
            actualType = "sequence";
        } else if (descriptor instanceof YMLScalarDescriptor) {
            actualType = "scalar";
        }

        if (null == actualType) {
            String message = "Unsupported descriptor type found";
            throw new IllegalStateException(message);
        }

        return actualType;
    }
}
