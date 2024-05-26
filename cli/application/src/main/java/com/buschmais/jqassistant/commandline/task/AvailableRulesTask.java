package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import org.apache.commons.cli.Options;

public class AvailableRulesTask extends AbstractRuleTask {

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
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
