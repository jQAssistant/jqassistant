package com.buschmais.jqassistant.scm.maven.configuration.mojo.converter;

import com.buschmais.jqassistant.core.rule.api.model.Severity;

import org.assertj.core.api.Assertions;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class SeverityConverterTest {

    private SeverityConverter converter = new SeverityConverter();

    @Test
    void canConvertReturnsTrueForSeverity() {
        boolean result = converter.canConvert(Severity.class);

        assertThat(result, equalTo(true));
    }

    @Test
    void canConvertReturnsFalseForString() {
        boolean result = converter.canConvert(String.class);

        assertThat(result, equalTo(false));
    }

    @Test
    void fromStringSucceedsForStringRepresentationOfSeverity() throws ComponentConfigurationException {
        Severity result = (Severity) converter.fromString(Severity.BLOCKER.name());

        assertThat(result, CoreMatchers.notNullValue());
        assertThat(result, equalTo(Severity.BLOCKER));
    }

    @Test
    void fromStringSucceedsForLowercaseStringRepresentationOfSeverity() throws ComponentConfigurationException {
        Severity result = (Severity) converter.fromString(Severity.BLOCKER.name().toLowerCase());

        assertThat(result, CoreMatchers.notNullValue());
        assertThat(result, equalTo(Severity.BLOCKER));
    }

    @Test
    void fromStringFailsForIllegalValue() {
        Assertions.assertThatThrownBy(() -> converter.fromString("OLIVER"))
                  .isInstanceOf(ComponentConfigurationException.class);
    }
}
