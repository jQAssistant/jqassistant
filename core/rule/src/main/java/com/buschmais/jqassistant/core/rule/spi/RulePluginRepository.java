package com.buschmais.jqassistant.core.rule.spi;

import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface RulePluginRepository extends LifecycleAware {

    @Override
    void initialize();

    @Override
    void destroy();

    /**
     * Get a list of sources providing rules.
     *
     * @return The list of sources providing rules.
     */
    List<RuleSource> getRuleSources();

    /**
     * Return the {@link RuleParserPlugin}s.
     *
     * @return The {@link RuleParserPlugin}s.
     */
    Collection<RuleParserPlugin> getRuleParserPlugins(RuleConfiguration ruleConfiguration) throws RuleException;

}
