package com.buschmais.jqassistant.scm.neo4jshell;

import org.neo4j.helpers.Service;
import org.neo4j.shell.*;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.scm.common.report.RuleHelper;

@Service.Implementation(App.class)
public class AvailableRulesApp extends AbstractJQAssistantApp {

    public AvailableRulesApp() throws PluginRepositoryException {
    }

    @Override
    public String getCommand() {
        return "available-rules";
    }

    @Override
    public Continuation execute(AppCommandParser parser, Session session, Output out) throws Exception {
        new RuleHelper(new ShellConsole(out)).printRuleSet(getAvailableRules());
        return Continuation.INPUT_COMPLETE;
    }
}
