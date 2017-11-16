package com.buschmais.jqassistant.scm.maven.configuration;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.converters.ConfigurationConverter;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * Custom component configurator which allows us to register our own
 * {@link ConfigurationConverter configuration converters}.
 */
@Component(role = ComponentConfigurator.class, hint = "custom")
public class CustomComponentConfigurator
    extends BasicComponentConfigurator
    implements Initializable {

    @Override
    public void initialize() throws InitializationException {
        this.converterLookup.registerConverter(new SeverityConfigurationConverter());
        this.converterLookup.registerConverter(new StoreLifeCycleConverter());
    }
}
