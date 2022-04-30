package com.buschmais.jqassistant.scm.maven.configuration.mojo;

import com.buschmais.jqassistant.scm.maven.StoreLifecycle;

import org.assertj.core.api.Assertions;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class StoreLifeCycleConverterTest {
    private StoreLifeCycleConverter converter = new StoreLifeCycleConverter();

    @Test
    void canConvertReturnsTrueForStoreLifecycle() {
        boolean result = converter.canConvert(StoreLifecycle.class);

        assertThat(result, equalTo(true));
    }

    @Test
    public void canConvertReturnsFalseForString() {
        boolean result = converter.canConvert(String.class);

        assertThat(result, equalTo(false));
    }

    @Test
    void fromStringSucceedsForStringRepresentation() throws Exception {
        StoreLifecycle result = (StoreLifecycle) converter.fromString(StoreLifecycle.MODULE.name());

        assertThat(result, CoreMatchers.notNullValue());
        assertThat(result, equalTo(StoreLifecycle.MODULE));
    }

    @Test
    void fromStringSucceedsForLowercaseStringRepresentationOfSeverity() throws Exception {
        StoreLifecycle result = (StoreLifecycle) converter.fromString(StoreLifecycle.MODULE.name().toLowerCase());

        assertThat(result, CoreMatchers.notNullValue());
        assertThat(result, equalTo(StoreLifecycle.MODULE));
    }

    @Test
    void fromStringFailsForIllegalValue() throws ComponentConfigurationException {
        Assertions.assertThatThrownBy(() ->converter.fromString("OLIVER"))
                  .isInstanceOf(ComponentConfigurationException.class);
    }

}
