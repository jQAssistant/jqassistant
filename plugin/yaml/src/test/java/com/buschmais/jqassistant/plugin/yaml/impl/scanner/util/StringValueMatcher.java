package com.buschmais.jqassistant.plugin.yaml.impl.scanner.util;

import com.buschmais.jqassistant.plugin.yaml.api.model.XXXX;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLValueDescriptor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;

// @todo Write some usefull tests! Oliver B. Fischer, 24th May 2015
public class StringValueMatcher extends TypeSafeMatcher<YAMLValueDescriptor> {

    private final String expectedValue;

    public StringValueMatcher(String expected) {
        expectedValue = expected;
    }

    @Override
    public boolean matchesSafely(YAMLValueDescriptor candidate) {
        boolean result = false;

        if (candidate.getValue() != null) {
            String value = candidate.getValue();

           result = expectedValue.equals(value);
        }

        return result;
    }

    public void describeTo(Description description) {
        description.appendText("does not match");
    }

    public static Matcher<YAMLValueDescriptor> hasValue(String expected) {
        return new StringValueMatcher(expected);
    }

}
