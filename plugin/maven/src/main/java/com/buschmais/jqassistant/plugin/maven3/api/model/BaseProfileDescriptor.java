package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Base descriptor for pom an profile.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
@Abstract
public interface BaseProfileDescriptor extends Descriptor {

    /**
     * Get sub modules.
     * 
     * @return The sub modules.
     */
    @Relation("HAS_MODULE")
    List<MavenModuleDescriptor> getModules();

    /**
     * Get defined properties.
     * 
     * @return The properties.
     */
    @Relation("HAS_PROPERTY")
    List<PropertyDescriptor> getProperties();

    /**
     * Get plugin information.
     * 
     * @return The plugins.
     */
    @Relation("USES_PLUGIN")
    List<MavenPluginDescriptor> getPlugins();

    /**
     * Get default plugin information to be made available for reference by
     * projects derived from this one. This plugin configuration will not be
     * resolved or bound to the lifecycle unless referenced. Any local
     * configuration for a given plugin will override the plugin's entire
     * definition here.
     * 
     * @return The managed plugins.
     */
    @Relation("MANAGES_PLUGIN")
    List<MavenPluginDescriptor> getManagedPlugins();
}
