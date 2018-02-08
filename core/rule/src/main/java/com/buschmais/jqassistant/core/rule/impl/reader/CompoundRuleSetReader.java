package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSetReader;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import static java.util.Arrays.asList;

/**
 * @author Michael Hunger
 */
public class CompoundRuleSetReader implements RuleSetReader {

    private List<? extends RuleSetReader> ruleSetReaders;

    public CompoundRuleSetReader(RuleConfiguration ruleConfiguration) {
        ruleSetReaders = asList(new XmlRuleSetReader(ruleConfiguration), new AsciiDocRuleSetReader(ruleConfiguration));
    }

    @Override
    public void read(List<? extends RuleSource> sources, RuleSetBuilder ruleSetBuilder) throws RuleException {
        for (RuleSetReader ruleSetReader : ruleSetReaders) {
            ruleSetReader.read(sources, ruleSetBuilder);
        }
    }
}
