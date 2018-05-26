package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

/**
 * @author Michael Hunger
 */
public class RuleParser {

    private Collection<? extends RuleParserPlugin> ruleParserPlugins;

    public RuleParser(Collection<RuleParserPlugin> ruleParserPlugins) {
        this.ruleParserPlugins = ruleParserPlugins;
    }

    public RuleSet parse(List<? extends RuleSource> sources) throws RuleException {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        for (RuleSource source : sources) {
            for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
                if (ruleParserPlugin.accepts(source)) {
                    ruleParserPlugin.read(source, ruleSetBuilder);
                }
            }
        }
        return ruleSetBuilder.getRuleSet();
    }
}
