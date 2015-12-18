package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;

import javax.xml.transform.Source;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Defines the interface of the rules reader.
 */
public interface RuleSetReader {

    /**
     * Reads the given {@link Source}s and a returns
     * {@link com.buschmais.jqassistant.core.analysis.api.rule.RuleSet}.
     * 
     * @param sources
     *            The sources to be read.
     */
    void read(List<? extends RuleSource> sources, RuleSetBuilder ruleSetBuilder) throws RuleException;

}
