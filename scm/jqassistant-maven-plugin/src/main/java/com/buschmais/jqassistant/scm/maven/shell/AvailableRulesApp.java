package com.buschmais.jqassistant.scm.maven.shell;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.scm.common.AnalysisHelper;
import org.neo4j.helpers.Service;
import org.neo4j.shell.*;

@Service.Implementation(App.class)
public class AvailableRulesApp extends AbstractJQAssistantApp {

    public AvailableRulesApp() throws PluginReaderException {
    }

    @Override
    public String getCommand() {
        return "available-rules";
    }

    @Override
    public Continuation execute(AppCommandParser parser, Session session, Output out) throws Exception {
        new AnalysisHelper(new ShellConsole(out)).printRuleSet(getAvailableRules());
        return Continuation.INPUT_COMPLETE;
    }
}
