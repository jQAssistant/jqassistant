package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.basic.AbstractBasicConverter;

public class SeverityConfigurationConverter extends AbstractBasicConverter {
    @Override
    public boolean canConvert(Class<?> type) {
        return type.isAssignableFrom(Severity.class);
    }

    @Override
    protected Object fromString(String str) throws ComponentConfigurationException {
        try {
            return Severity.fromValue(str);
        }
        catch (RuleException re) {
            throw new ComponentConfigurationException("Unknown or illegal value for severity.", re);
        }
    }
}
