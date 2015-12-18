package com.buschmais.jqassistant.core.analysis.api;

import java.util.Arrays;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.AsciiDocRuleSetReader;
import com.buschmais.jqassistant.core.analysis.impl.XmlRuleSetReader;

/**
 * @author mh
 * @since 12.10.14
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
