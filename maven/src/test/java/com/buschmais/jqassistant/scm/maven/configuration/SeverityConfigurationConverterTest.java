package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

import org.assertj.core.api.Assertions;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SeverityConfigurationConverterTest {

    private SeverityConfigurationConverter converter = new SeverityConfigurationConverter();

    @Test
    public void canConvertReturnsTrueForSeverity() {
        boolean result = converter.canConvert(Severity.class);

        assertThat(result, equalTo(true));
    }

    @Test
    public void canConvertReturnsFalseForString() {
        boolean result = converter.canConvert(String.class);

        assertThat(result, equalTo(false));
    }

    @Test
    public void fromStringSucceedsForStringRepresentationOfSeverity() throws ComponentConfigurationException {
        Severity result = (Severity) converter.fromString(Severity.BLOCKER.name());

        assertThat(result, CoreMatchers.notNullValue());
        assertThat(result, equalTo(Severity.BLOCKER));
    }

    @Test
    public void fromStringSucceedsForLowercaseStringRepresentationOfSeverity() throws ComponentConfigurationException {
        Severity result = (Severity) converter.fromString(Severity.BLOCKER.name().toLowerCase());

        assertThat(result, CoreMatchers.notNullValue());
        assertThat(result, equalTo(Severity.BLOCKER));
    }

    @Test
    public void fromStringFailsForIllegalValue() {
        Assertions.assertThatThrownBy(() -> converter.fromString("OLIVER"))
                  .isInstanceOf(ComponentConfigurationException.class);
    }
}
