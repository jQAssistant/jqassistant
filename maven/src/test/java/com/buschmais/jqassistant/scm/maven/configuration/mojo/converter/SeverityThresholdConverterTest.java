package com.buschmais.jqassistant.scm.maven.configuration.mojo.converter;

import com.buschmais.jqassistant.core.rule.api.model.Severity;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class SeverityThresholdConverterTest {

    private SeverityThresholdConverter converter = new SeverityThresholdConverter();

    @Test
    void canConvert() {
        assertThat(converter.canConvert(Severity.Threshold.class), equalTo(true));
        assertThat(converter.canConvert(Severity.class), equalTo(false));
    }

    @Test
    void severity() throws ComponentConfigurationException {
        assertThat(converter.fromString(BLOCKER.name())
            .getThreshold(), equalTo(of(BLOCKER)));
        assertThat(converter.fromString(CRITICAL.name())
            .getThreshold(), equalTo(of(CRITICAL)));
        assertThat(converter.fromString(MAJOR.name())
            .getThreshold(), equalTo(of(MAJOR)));
        assertThat(converter.fromString(MINOR.name())
            .getThreshold(), equalTo(of(MINOR)));
        assertThat(converter.fromString(INFO.name())
            .getThreshold(), equalTo(of(INFO)));
    }

    @Test
    void never() throws ComponentConfigurationException {
        assertThat(converter.fromString("never")
            .getThreshold(), equalTo(empty()));
        assertThat(converter.fromString("NEVER")
            .getThreshold(), equalTo(empty()));
    }

    @Test
    void invalidValue() {
        try {
            converter.fromString("invalid value");
            fail("Expecting a " + ComponentConfigurationException.class);
        } catch (ComponentConfigurationException e) {
            assertThat(e.getMessage(), containsString("invalid value"));
        }
    }
}
