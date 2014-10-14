package com.buschmais.jqassistant.scm.neo4jserver.impl;

import static java.util.Collections.emptyList;

import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * The customized Neo4j server.
 * <p>
 * The class adds the {@link JQAServerModule}
 * </p>
 */
public class DefaultServerImpl extends AbstractServer {

    private ScannerPluginRepository scannerPluginRepository;

    private RulePluginRepository rulePluginRepository;

    /**
     * Constructor.
     * 
     * @param graphStore
     *            The store instance to use.
     * @param scannerPluginRepository
     *            The scanner plugin repository.
     * @param rulePluginRepository
     *            The rule plugin repository.
     */
    public DefaultServerImpl(EmbeddedGraphStore graphStore, ScannerPluginRepository scannerPluginRepository, RulePluginRepository rulePluginRepository) {
        super(graphStore);
        this.scannerPluginRepository = scannerPluginRepository;
        this.rulePluginRepository = rulePluginRepository;
    }

    @Override
    protected Iterable<? extends Class<?>> getExtensions() {
        return emptyList();
    }

    @Override
    protected ScannerPluginRepository getScannerPluginRepository() {
        return scannerPluginRepository;
    }

    @Override
    protected RulePluginRepository getRulePluginRepository() {
        return rulePluginRepository;
    }

}
