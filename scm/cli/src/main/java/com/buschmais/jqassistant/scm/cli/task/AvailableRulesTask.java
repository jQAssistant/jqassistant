package com.buschmais.jqassistant.scm.cli.task;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;

public class AvailableRulesTask extends AbstractAnalyzeTask {

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     */
    public AvailableRulesTask(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

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
