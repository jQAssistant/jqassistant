package com.buschmais.jqassistant.scm.cli;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

public class AvailableRulesTask extends AbstractAnalyzeTask {

    @Override
    protected void executeTask(Store store) {
        RuleSet availableRules = getAvailableRules();
        reportHelper.printRuleSet(availableRules);
    }
}
