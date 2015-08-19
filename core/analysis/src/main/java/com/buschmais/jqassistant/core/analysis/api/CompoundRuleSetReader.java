package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.source.CompoundRuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.AsciiDocRuleSetReader;
import com.buschmais.jqassistant.core.analysis.impl.XmlRuleSetReader;

/**
 * @author mh
 * @since 12.10.14
 */
public class CompoundRuleSetReader implements RuleSetReader {

    @Override
    public RuleSet read(List<? extends RuleSource> sources) throws RuleException {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        RuleSetReader xmlReader = new XmlRuleSetReader(ruleSetBuilder);
        RuleSet xmlRuleSet = xmlReader.read(sources);
        RuleSetReader adocReader = new AsciiDocRuleSetReader(ruleSetBuilder);
        RuleSet adocRuleSet = adocReader.read(sources);
        return new CompoundRuleSet(xmlRuleSet, adocRuleSet);
    }

}
