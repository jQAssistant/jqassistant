package com.buschmais.jqassistant.scm.cli;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

public class EffectiveRulesTask extends AbstractAnalyzeTask {

    @Override
    protected void executeTask(Store store) {
        RuleSet availableRules = getEffectiveRules();
        reportHelper.printRuleSet(availableRules);
    }
}
