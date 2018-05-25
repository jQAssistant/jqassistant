package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSourceReader;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import static java.util.Arrays.asList;

/**
 * @author Michael Hunger
 */
public class RuleCollector {

    private List<? extends RuleSourceReader> ruleSourceReaders;

    public RuleCollector(RuleConfiguration ruleConfiguration) throws RuleException {
        ruleSourceReaders = asList(new XmlRuleSourceReader(), new AsciiDocRuleSourceReader());
        for (RuleSourceReader ruleSourceReader : ruleSourceReaders) {
            ruleSourceReader.initialize();
            ruleSourceReader.configure(ruleConfiguration);
        }
    }

    public RuleSet read(List<? extends RuleSource> sources) throws RuleException {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        for (RuleSource source : sources) {
            for (RuleSourceReader ruleSourceReader : ruleSourceReaders) {
                if (ruleSourceReader.accepts(source)) {
                    ruleSourceReader.read(source, ruleSetBuilder);
                }
            }
        }
        return ruleSetBuilder.getRuleSet();
    }
}
