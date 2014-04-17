package com.buschmais.jqassistant.mojo.shell;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import org.neo4j.helpers.Service;
import org.neo4j.shell.*;

import java.util.Collections;

@Service.Implementation(App.class)
public class AnalyzeApp extends AbstractJQAssistantApp {

    private RuleSet availableRules;

    public AnalyzeApp() throws PluginReaderException {
        super();
        availableRules = readRuleSet();
    }

    @Override
    public String getCommand() {
        return "analyze";
    }

    @Override
    public Continuation execute(AppCommandParser parser, Session session, Output out) throws Exception {
        getStore().start(Collections.<Class<?>>emptyList());
        out.println("Start analysis");
        for (String s : parser.arguments()) {
            out.println(s);
        }
        out.println("Stop analysis");
        getStore().stop();
        return Continuation.INPUT_COMPLETE;
    }


}
