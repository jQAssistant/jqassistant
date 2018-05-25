package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSourceReaderPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

/**
 * @author Michael Hunger
 */
public class RuleCollector {

    private Collection<? extends RuleSourceReaderPlugin> ruleSourceReaderPlugins;

    public RuleCollector(Collection<RuleSourceReaderPlugin> ruleSourceReaderPlugins) {
        this.ruleSourceReaderPlugins = ruleSourceReaderPlugins;
    }

    public RuleSet read(List<? extends RuleSource> sources) throws RuleException {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        for (RuleSource source : sources) {
            for (RuleSourceReaderPlugin ruleSourceReader : ruleSourceReaderPlugins) {
                if (ruleSourceReader.accepts(source)) {
                    ruleSourceReader.read(source, ruleSetBuilder);
                }
            }
        }
        return ruleSetBuilder.getRuleSet();
    }
}
