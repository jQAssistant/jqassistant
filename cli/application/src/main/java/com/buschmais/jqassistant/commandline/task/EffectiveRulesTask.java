package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

public class EffectiveRulesTask extends AbstractAnalyzeTask {


    @Override
    protected void executeTask(Configuration configuration, Store store) throws CliExecutionException {
        try {
            Rule rule = configuration.analyze()
                .rule();
            RuleSet availableRules = getAvailableRules(rule);
            ruleHelper.printRuleSet(availableRules, getRuleSelection(availableRules), rule);
        } catch (RuleException e) {
            throw new CliExecutionException("Cannot print rules.", e);
        }
    }
}
