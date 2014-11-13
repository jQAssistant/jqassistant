package com.buschmais.jqassistant.scm.cli.task;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;

public class EffectiveRulesTask extends AbstractAnalyzeTask {

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     */
    public EffectiveRulesTask(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

    @Override
    protected void executeTask(Store store) throws CliExecutionException {
        try {
            RuleSet availableRules = getAvailableRules();
            ruleHelper.printRuleSet(availableRules, getRuleSelection(availableRules));
        } catch (AnalysisException e) {
            throw new CliExecutionException("Cannot print rules.", e);
        }
    }
}
