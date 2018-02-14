package com.buschmais.jqassistant.scm.maven.configuration;

import java.util.Locale;

import com.buschmais.jqassistant.scm.maven.StoreLifecycle;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.converters.basic.AbstractBasicConverter;

import static java.lang.String.format;

public class StoreLifeCycleConverter extends AbstractBasicConverter {
    @Override
    public boolean canConvert(Class<?> type) {
        return type.isAssignableFrom(StoreLifecycle.class);
    }

    @Override
    protected Object fromString(String value) throws ComponentConfigurationException {
        StoreLifecycle result = null;

        for (StoreLifecycle sl : StoreLifecycle.values()) {
            if (sl.name().toLowerCase(Locale.ENGLISH).equals(value.toLowerCase(Locale.ENGLISH))) {
                result = sl;
            }
        }

        if (null == result) {
            String message = format("'%s' is not a known store lifecyle.", value);
            throw new ComponentConfigurationException(message);
        }

        return result;
    }

}
