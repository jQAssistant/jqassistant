package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

public class EffectiveRulesTask extends AbstractAnalyzeTask {


    @Override
    protected void executeTask(Configuration configuration, Store store) throws CliExecutionException {
        try {
            RuleSet availableRules = getAvailableRules();
            ruleHelper.printRuleSet(availableRules, getRuleSelection(availableRules));
        } catch (RuleException e) {
            throw new CliExecutionException("Cannot print rules.", e);
        }
    }
}
