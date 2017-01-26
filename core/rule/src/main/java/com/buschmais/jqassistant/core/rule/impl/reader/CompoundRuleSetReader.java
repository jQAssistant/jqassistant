package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.Arrays;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSetReader;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

/**
 * @author Michael Hunger
 */
public class CompoundRuleSetReader implements RuleSetReader {

    private List<RuleSetReader> ruleSetReaders = Arrays.asList(new XmlRuleSetReader(), new AsciiDocRuleSetReader());

    @Override
    public void read(List<? extends RuleSource> sources, RuleSetBuilder ruleSetBuilder) throws RuleException {
        for (RuleSetReader ruleSetReader : ruleSetReaders) {
            ruleSetReader.read(sources, ruleSetBuilder);
        }
    }

}
