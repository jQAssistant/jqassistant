package com.buschmais.jqassistant.scm.maven.shell;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import org.neo4j.helpers.Service;
import org.neo4j.shell.*;

@Service.Implementation(App.class)
public class AvailableRulesApp extends AbstractJQAssistantApp {

    private RuleSet availableRules;

    public AvailableRulesApp() throws PluginReaderException {
        super();
        availableRules = readRuleSet();
    }

    @Override
    public String getCommand() {
        return "available-rules";
    }

    @Override
    public Continuation execute(AppCommandParser parser, Session session, Output out) throws Exception {
        printRuleSet(availableRules, out);
        return Continuation.INPUT_COMPLETE;
    }


}
