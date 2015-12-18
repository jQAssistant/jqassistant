package com.buschmais.jqassistant.plugin.yaml.impl.scanner.util;

import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLValueDescriptor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class StringValueMatcherTest {

    @Test
    public void matcherReturnTrueIfYAMLValueDescriptorHasRequiredStringValue() {
        YAMLValueDescriptor mockedYVD = Mockito.mock(YAMLValueDescriptor.class);

        Mockito.when(mockedYVD.getValue()).thenReturn("ABC");

        StringValueMatcher matcher = new StringValueMatcher("ABC");

        assertThat(matcher.matchesSafely(mockedYVD), is(true));
    }

    @Test
    public void matcherReturnTrueIfYAMLValueDescriptorHasNotRequiredStringValue() {
        YAMLValueDescriptor mockedYVD = Mockito.mock(YAMLValueDescriptor.class);

        Mockito.when(mockedYVD.getValue()).thenReturn("CBA");

        StringValueMatcher matcher = new StringValueMatcher("ABC");

        assertThat(matcher.matchesSafely(mockedYVD), is(false));
    }

    @Test
    public void matcherCanBeUsedInConjunctionWithCollectionAndHasItem() {
        YAMLValueDescriptor valueA = Mockito.mock(YAMLValueDescriptor.class);
        YAMLValueDescriptor valueB = Mockito.mock(YAMLValueDescriptor.class);

        Mockito.when(valueA.getValue()).thenReturn("A");
        Mockito.when(valueB.getValue()).thenReturn("B");

        List<YAMLValueDescriptor> list = new ArrayList<>(2);

        list.add(valueA);
        list.add(valueB);

        Matcher<Iterable<? super YAMLValueDescriptor>> matcher = Matchers.hasItem(StringValueMatcher.hasValue("A"));

        assertThat(list, matcher);
    }

    @Test
    public void matcherFailsIfAGivenCollectionDoesNotContainASingleMatch() {
        YAMLValueDescriptor valueA = Mockito.mock(YAMLValueDescriptor.class);
        YAMLValueDescriptor valueB = Mockito.mock(YAMLValueDescriptor.class);

        Mockito.when(valueA.getValue()).thenReturn("A");
        Mockito.when(valueB.getValue()).thenReturn("B");

        List<YAMLValueDescriptor> list = new ArrayList<>(2);

        list.add(valueA);
        list.add(valueB);

        Matcher<Iterable<? super YAMLValueDescriptor>> matcher = Matchers.hasItem(StringValueMatcher.hasValue("C"));

        assertThat(list, not(matcher));
    }
}