package com.buschmais.jqassistant.core.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

/**
 * The plugin repository.
 */
public class PluginRepositoryImpl implements PluginRepository {

    private PluginConfigurationReader pluginConfigurationReader;

    private StorePluginRepository storePluginRepository;
    private ScannerPluginRepository scannerPluginRepository;
    private RulePluginRepository rulePluginRepository;
    private RuleParserPluginRepository ruleParserPluginRepository;
    private AnalyzerPluginRepository analyzerPluginRepository;

    private ClassLoader classLoader;

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     *            The plugin configuration reader.
     */
    public PluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        this.pluginConfigurationReader = pluginConfigurationReader;
    }

    @Override
    public void initialize() {
        this.storePluginRepository = new StorePluginRepositoryImpl(pluginConfigurationReader);
        this.scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader);
        this.scannerPluginRepository.initialize();
        this.rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        this.ruleParserPluginRepository = new RuleParserPluginRepositoryImpl(pluginConfigurationReader);
        this.ruleParserPluginRepository.initialize();
        this.analyzerPluginRepository = new AnalyzerPluginRepositoryImpl(pluginConfigurationReader);
        this.analyzerPluginRepository.initialize();
        this.classLoader = pluginConfigurationReader.getClassLoader();
    }

    @Override
    public void destroy() {
        if (scannerPluginRepository != null) {
            this.scannerPluginRepository.destroy();
        }
        if (ruleParserPluginRepository != null) {
            this.ruleParserPluginRepository.destroy();
        }
        if (analyzerPluginRepository != null) {
            this.analyzerPluginRepository.destroy();
        }
    }

    @Override
    public StorePluginRepository getStorePluginRepository() {
        return storePluginRepository;
    }

    @Override
    public ScannerPluginRepository getScannerPluginRepository() {
        return scannerPluginRepository;
    }

    @Override
    public RulePluginRepository getRulePluginRepository() {
        return rulePluginRepository;
    }

    @Override
    public RuleParserPluginRepository getRuleParserPluginRepository() {
        return ruleParserPluginRepository;
    }

    @Override
    public AnalyzerPluginRepository getAnalyzerPluginRepository() {
        return analyzerPluginRepository;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
