package com.buschmais.jqassistant.scm.neo4jshell;

import org.neo4j.helpers.Service;
import org.neo4j.shell.*;

import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.scm.common.report.RuleHelper;

@Service.Implementation(App.class)
public class EffectiveRulesApp extends AbstractJQAssistantApp {

    public EffectiveRulesApp() throws PluginRepositoryException {
    }

    @Override
    public String getCommand() {
        return "effective-rules";
    }

    @Override
    public Continuation execute(AppCommandParser parser, Session session, Output out) throws Exception {
        RuleSet availableRules = getAvailableRules();
        RuleSelection ruleSelection = selectRules(parser);
        new RuleHelper(new ShellConsole(out)).printRuleSet(availableRules, ruleSelection);
        return Continuation.INPUT_COMPLETE;
    }
}
