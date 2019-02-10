package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Hunger
 */
public class RuleParser {
    private final Logger logger = LoggerFactory.getLogger(RuleParser.class);

    private Collection<? extends RuleParserPlugin> ruleParserPlugins;

    public RuleParser(Collection<RuleParserPlugin> ruleParserPlugins) {
        this.ruleParserPlugins = ruleParserPlugins;
    }

    public RuleSet parse(List<? extends RuleSource> sources) throws RuleException {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        for (RuleSource source : sources) {
            boolean accepted = false;
            for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
                if (ruleParserPlugin.accepts(source)) {
                    accepted = true;
                    ruleParserPlugin.parse(source, ruleSetBuilder);
                }
            }

            if (!accepted) {
                logger.warn("Rule source with id '{}' has not been processed by any rule parser. " +
                            "Contained rules are not available to jQAssistant.", source.getId());
            }
        }
        return ruleSetBuilder.getRuleSet();
    }
}
