package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

public class EffectiveRulesTask extends AbstractAnalyzeTask {

    @Override
    public void run(CliConfiguration configuration) throws CliExecutionException {
        try {
            Analyze analyze = configuration.analyze();
            Rule rule = analyze
                .rule();
            RuleSet availableRules = getAvailableRules(rule);
            ruleHelper.printRuleSet(availableRules, getRuleSelection(availableRules, analyze), rule);
        } catch (RuleException e) {
            throw new CliExecutionException("Cannot print rules.", e);
        }
    }
}
