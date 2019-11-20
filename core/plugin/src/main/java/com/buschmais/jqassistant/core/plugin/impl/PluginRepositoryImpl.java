package com.buschmais.jqassistant.core.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.*;
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
    private RuleInterpreterPluginRepository ruleInterpreterPluginRepository;
    private RuleParserPluginRepository ruleParserPluginRepository;
    private ReportPluginRepository reportPluginRepository;

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
        this.ruleInterpreterPluginRepository = new RuleInterpreterPluginRepositoryImpl(pluginConfigurationReader);
        this.ruleInterpreterPluginRepository.initialize();
        this.ruleParserPluginRepository = new RuleParserPluginRepositoryImpl(pluginConfigurationReader);
        this.ruleParserPluginRepository.initialize();
        this.reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader);
        this.reportPluginRepository.initialize();
        this.classLoader = pluginConfigurationReader.getClassLoader();
    }

    @Override
    public void destroy() {
        if (scannerPluginRepository != null) {
            this.scannerPluginRepository.destroy();
        }
        if (ruleInterpreterPluginRepository != null) {
            this.ruleInterpreterPluginRepository.destroy();
        }
        if (ruleParserPluginRepository != null) {
            this.ruleParserPluginRepository.destroy();
        }
        if (reportPluginRepository != null) {
            this.reportPluginRepository.destroy();
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
    public RuleInterpreterPluginRepository getRuleInterpreterPluginRepository() {
        return ruleInterpreterPluginRepository;
    }

    @Override
    public RuleParserPluginRepository getRuleParserPluginRepository() {
        return ruleParserPluginRepository;
    }

    @Override
    public ReportPluginRepository getReportPluginRepository() {
        return reportPluginRepository;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
