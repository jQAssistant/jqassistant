package com.buschmais.jqassistant.scm.maven.configuration.mojo;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.basic.AbstractBasicConverter;

import static java.lang.String.format;

public class SeverityConfigurationConverter extends AbstractBasicConverter {
    @Override
    public boolean canConvert(Class<?> type) {
        return type.isAssignableFrom(Severity.class);
    }

    @Override
    protected Object fromString(String value) throws ComponentConfigurationException {
        try {
            return Severity.fromValue(value);
        }
        catch (RuleException re) {
            String message = format("'%s' is not a known severity.", value);
            throw new ComponentConfigurationException(message);
        }
    }
}
