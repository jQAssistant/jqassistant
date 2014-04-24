package com.buschmais.jqassistant.core.pluginmanager.api;

import java.util.List;

import javax.xml.transform.Source;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface RulePluginRepository extends PluginRepository {

    /**
     * Get a list of sources providing rules.
     * 
     * @return The list of sources providing rules.
     */
    List<Source> getRuleSources();

}
