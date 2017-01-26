package com.buschmais.jqassistant.core.plugin.api;

import java.util.List;

import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface RulePluginRepository {

    /**
     * Get a list of sources providing rules.
     * 
     * @return The list of sources providing rules.
     */
    List<RuleSource> getRuleSources();

}
