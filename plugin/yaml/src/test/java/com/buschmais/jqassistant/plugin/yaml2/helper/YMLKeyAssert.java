package com.buschmais.jqassistant.plugin.yaml2.helper;

import java.util.Objects;
import java.util.function.Function;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.assertj.core.api.AbstractObjectAssert;

// todo rename to YMLSimpleKeyAssert
public class YMLKeyAssert extends AbstractObjectAssert<YMLKeyAssert, YMLSimpleKeyDescriptor> {

    public YMLKeyAssert(YMLSimpleKeyDescriptor descriptor) {
        super(descriptor, YMLKeyAssert.class);
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
        isNotNull();
        hasValue();

        String assertjErrorMessage = "\nExpecting key descriptor to have a <%s> as value\n" +
                                     "but it has a <%s> as value\n";

        YMLDescriptor value = actual.getValue();

        if (!(value instanceof YMLScalarDescriptor)) {
            String actualType = ((Function<YMLDescriptor, String>) descriptor -> {
                String type = "???";

                if (descriptor instanceof YMLMapDescriptor) {
                    type = "map";
                } else if (descriptor instanceof YMLSequenceDescriptor) {
                    type = "sequence";
                } else if (descriptor instanceof YMLScalarDescriptor) {
                    type = "scalar";
                }

                return type;
            }).apply(actual);

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
        isNotNull();
        hasValue();

        String assertjErrorMessage = "\nExpecting key descriptor to have a <%s> as value\n" +
                                     "but it has a <%s> as value\n";

        YMLDescriptor value = actual.getValue();

        if (!(value instanceof YMLMapDescriptor)) {
            String actualType = ((Function<YMLDescriptor, String>) descriptor -> {
                String type = "???";

                if (descriptor instanceof YMLMapDescriptor) {
                    type = "map";
                } else if (descriptor instanceof YMLSequenceDescriptor) {
                    type = "sequence";
                } else if (descriptor instanceof YMLScalarDescriptor) {
                    type = "scalar";
                }

                return type;
            }).apply(actual);

            failWithMessage(assertjErrorMessage, "map", actualType);
        }

        return this;
    }

    public YMLKeyAssert hasSequenceAsValue() {
        isNotNull();
        hasValue();

        String assertjErrorMessage = "\nExpecting key descriptor to have a <%s> as value\n" +
                                     "but it has a <%s> as value\n";

        YMLDescriptor value = actual.getValue();

        // todo extract method
        if (!(value instanceof YMLSequenceDescriptor)) {
            String actualType = ((Function<YMLDescriptor, String>) descriptor -> {
                String type = "???";

                if (descriptor instanceof YMLMapDescriptor) {
                    type = "map";
                } else if (descriptor instanceof YMLSequenceDescriptor) {
                    type = "sequence";
                } else if (descriptor instanceof YMLScalarDescriptor) {
                    type = "scalar";
                }

                return type;
            }).apply(actual);

            failWithMessage(assertjErrorMessage, "sequence", actualType);
        }

        return this;
    }
}
