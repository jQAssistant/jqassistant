package com.buschmais.jqassistant.plugin.java.test.matcher;

import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;

import org.hamcrest.Matcher;

import static org.hamcrest.core.IsEqual.equalTo;

public class PropertyDescriptorMatcher<T extends PropertyDescriptor>
    extends ValueDescriptorMatcher<T> {

    protected PropertyDescriptorMatcher(String expectedName, Matcher<T> valueMatcher) {
        super(expectedName, valueMatcher, "property");
    }

    public static Matcher<? super PropertyDescriptor> propertyDescriptor(String keyName, String expectedValue) {
        return new PropertyDescriptorMatcher(keyName, equalTo(expectedValue));
    }
}
