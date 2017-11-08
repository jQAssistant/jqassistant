package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

public class AvailableRulesTask extends AbstractAnalyzeTask {


    @Override
    protected void executeTask(Store store) throws CliExecutionException {
        RuleSet availableRules = getAvailableRules();
        try {
            ruleHelper.printRuleSet(availableRules);
        } catch (RuleException e) {
            throw new CliExecutionException("Cannot print available rules.", e);
        }
    }
}
