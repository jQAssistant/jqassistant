package com.buschmais.jqassistant.scm.cli.task;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;

public class AvailableRulesTask extends AbstractAnalyzeTask {


    @Override
    protected void executeTask(Store store) throws CliExecutionException {
        RuleSet availableRules = getAvailableRules();
        try {
            ruleHelper.printRuleSet(availableRules);
        } catch (AnalysisException e) {
            throw new CliExecutionException("Cannot print available rules.", e);
        }
    }
}
