package com.buschmais.jqassistant.scm.maven.shell;

import org.neo4j.helpers.Service;
import org.neo4j.shell.*;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.AnalysisHelper;

@Service.Implementation(App.class)
public class AnalyzeApp extends AbstractJQAssistantApp {

    public AnalyzeApp() throws PluginReaderException {
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
        Analyzer analyzer = new AnalyzerImpl(store, reportWriter);
        analyzer.execute(effectiveRuleSet);
        AnalysisHelper analysisHelper = new AnalysisHelper(new ShellConsole(out));
        analysisHelper.verifyConceptResults(reportWriter);
        analysisHelper.verifyConstraintViolations(reportWriter);
        store.stop();
        return Continuation.INPUT_COMPLETE;
    }

}
