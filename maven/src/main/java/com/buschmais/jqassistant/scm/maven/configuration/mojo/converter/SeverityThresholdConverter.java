package com.buschmais.jqassistant.scm.maven.configuration.mojo.converter;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.basic.AbstractBasicConverter;

import static java.lang.String.format;

public class SeverityThresholdConverter extends AbstractBasicConverter {
    @Override
    public boolean canConvert(Class<?> type) {
        return type.isAssignableFrom(Severity.Threshold.class);
    }

    @Override
    protected Severity.Threshold fromString(String value) throws ComponentConfigurationException {
        try {
            return Severity.Threshold.from(value);
        } catch (RuleException re) {
            String message = format("'%s' is not a known severity threshold.", value);
            throw new ComponentConfigurationException(message);
        }
    }
}
