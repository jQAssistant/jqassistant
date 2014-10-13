package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;

import javax.xml.transform.Source;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSource;

/**
 * Defines the interface of the rules reader.
 */
public interface RuleSetReader {

    /**
     * Reads the given {@link Source}s and a returns {@link RuleSet}.
     * 
     * @param sources
     *            The sources to be read.
     * @return The map of constraint groups.
     */
    public RuleSet read(List<RuleSource> sources);

}
