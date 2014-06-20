package com.buschmais.jqassistant.scm.neo4jshell;

import org.neo4j.helpers.Service;
import org.neo4j.shell.App;
import org.neo4j.shell.AppCommandParser;
import org.neo4j.shell.Continuation;
import org.neo4j.shell.Output;
import org.neo4j.shell.Session;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;

@Service.Implementation(App.class)
public class AnalyzeApp extends AbstractJQAssistantApp {

    public AnalyzeApp() throws PluginRepositoryException {
    }

    @Override
    public String getCommand() {
        return "analyze";
    }

    @Override
    public Continuation execute(AppCommandParser parser, Session session, final Output out) throws Exception {
        RuleSet effectiveRuleSet = getEffectiveRuleSet(parser);
        InMemoryReportWriter reportWriter = new InMemoryReportWriter();
        Store store = getStore();
        store.start(getScannerPluginRepository().getDescriptorTypes());
        ShellConsole console = new ShellConsole(out);
        Analyzer analyzer = new AnalyzerImpl(store, reportWriter, console);
        analyzer.execute(effectiveRuleSet);
        ReportHelper reportHelper = new ReportHelper(console);
        reportHelper.verifyConceptResults(reportWriter);
        reportHelper.verifyConstraintViolations(reportWriter);
        store.stop();
        return Continuation.INPUT_COMPLETE;
    }

}
