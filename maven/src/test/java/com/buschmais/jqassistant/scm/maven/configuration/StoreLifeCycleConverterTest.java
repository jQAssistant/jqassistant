package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.scm.maven.StoreLifecycle;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class StoreLifeCycleConverterTest {
    private StoreLifeCycleConverter converter = new StoreLifeCycleConverter();

    @Test
    public void canConvertReturnsTrueForStoreLifecycle() {
        boolean result = converter.canConvert(StoreLifecycle.class);

        assertThat(result, equalTo(true));
    }

    @Test
    public void canConvertReturnsFalseForString() {
        boolean result = converter.canConvert(String.class);

        assertThat(result, equalTo(false));
    }

    @Test
    public void fromStringSucceedsForStringRepresentation() throws Exception {
        StoreLifecycle result = (StoreLifecycle) converter.fromString(StoreLifecycle.MODULE.name());

        assertThat(result, CoreMatchers.notNullValue());
        assertThat(result, equalTo(StoreLifecycle.MODULE));
    }

    @Test
    public void fromStringSucceedsForLowercaseStringRepresentationOfSeverity() throws Exception {
        StoreLifecycle result = (StoreLifecycle) converter.fromString(StoreLifecycle.MODULE.name().toLowerCase());

        assertThat(result, CoreMatchers.notNullValue());
        assertThat(result, equalTo(StoreLifecycle.MODULE));
    }

    @Test(expected = ComponentConfigurationException.class)
    public void fromStringFailsForIllegalValue() throws ComponentConfigurationException {
        converter.fromString("OLIVER");
    }

}