package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.model.RuleSetBuilder;
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
            parse(source, ruleSetBuilder);
        }
        return ruleSetBuilder.getRuleSet();
    }

    private void parse(RuleSource source, RuleSetBuilder ruleSetBuilder) throws RuleException {
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            if (ruleParserPlugin.accepts(source)) {
                logger.debug("Parsing rule source with id '{}' using '{}'.", source.getId(), ruleParserPlugin);
                ruleParserPlugin.parse(source, ruleSetBuilder);
                return;
            }
        }
        logger.debug("Rule source with id '{}' has not been accepted by any rule parser.", source.getId());
    }
}
