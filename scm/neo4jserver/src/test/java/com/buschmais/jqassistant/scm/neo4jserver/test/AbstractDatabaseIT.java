package com.buschmais.jqassistant.scm.neo4jserver.test;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

import java.io.IOException;

public abstract class AbstractDatabaseIT {

    private static final String STORE_DIR = "D:\\dev\\jqassistant_core\\core\\target\\jqassistant\\store";

    protected RuleSet ruleSet;

    protected InMemoryReportWriter reportWriter;


    protected void reset() throws IOException {
        /*FileUtils.deleteDirectory(new File(STORE_DIR));*/
    }

    protected EmbeddedGraphStore createStore() {
        return new EmbeddedGraphStore(STORE_DIR);
    }


}
