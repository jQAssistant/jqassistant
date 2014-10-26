package com.buschmais.jqassistant.scm.cli.task;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;

public class EffectiveRulesTask extends AbstractAnalyzeTask {

    @Override
    protected void executeTask(Store store) throws CliExecutionException {
        RuleSet availableRules = getEffectiveRules();
        reportHelper.printRuleSet(availableRules);
    }
}
