package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import org.apache.commons.cli.Options;

@Description("Lists all effective rules.")
public class EffectiveRulesTask extends AbstractRuleTask {

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
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
