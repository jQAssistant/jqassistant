package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

public class AvailableRulesTask extends AbstractAnalyzeTask {

    @Override
    protected void executeTask(CliConfiguration configuration, Store store) throws CliExecutionException {
        RuleSet availableRules = getAvailableRules(configuration.analyze()
            .rule());
        try {
            ruleHelper.printRuleSet(availableRules, configuration.analyze()
                .rule());
        } catch (RuleException e) {
            throw new CliExecutionException("Cannot print available rules.", e);
        }
    }
}
