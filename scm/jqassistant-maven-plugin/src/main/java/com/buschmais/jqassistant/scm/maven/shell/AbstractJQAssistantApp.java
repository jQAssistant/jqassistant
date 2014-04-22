package com.buschmais.jqassistant.scm.maven.shell;

import java.rmi.RemoteException;
import java.util.List;

import javax.xml.transform.Source;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.shell.AppShellServer;
import org.neo4j.shell.impl.AbstractApp;
import org.neo4j.shell.kernel.GraphDatabaseShellServer;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.pluginmanager.api.RulePluginRepository;
import com.buschmais.jqassistant.core.pluginmanager.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.GraphDbStore;
import com.buschmais.jqassistant.scm.common.AnalysisHelper;

/**
 * Abstract base implementation for shell commands.
 */
public abstract class AbstractJQAssistantApp extends AbstractApp {

    /**
     * The rules reader instance.
     */
    private RuleSetReader ruleSetReader;
    private RulePluginRepository rulePluginRepository;

    private Store store = null;

    AbstractJQAssistantApp() throws PluginReaderException {
        rulePluginRepository = new RulePluginRepositoryImpl();
        ruleSetReader = new RuleSetReaderImpl();

    }

    @Override
    public final String getName() {
        return "jqa:" + getCommand();
    }

    protected abstract String getCommand();

    protected RuleSet readRuleSet() {
        List<Source> ruleSources = rulePluginRepository.getRuleSources();
        return ruleSetReader.read(ruleSources);
    }

    protected Store getStore() {
        if (store == null) {
            AppShellServer server = getServer();
            if (!(server instanceof GraphDatabaseShellServer)) {
                throw new IllegalStateException("Unexpected server type " + server);
            }
            GraphDatabaseService db = ((GraphDatabaseShellServer) server).getDb();
            store = new GraphDbStore(db);
        }
        return store;
    }

}
