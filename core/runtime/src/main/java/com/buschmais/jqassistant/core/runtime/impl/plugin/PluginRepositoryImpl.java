package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginInfo;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import static java.util.Optional.ofNullable;

/**
 * The plugin repository.
 */
public class PluginRepositoryImpl implements PluginRepository {

    private PluginConfigurationReader pluginConfigurationReader;

    private StorePluginRepository storePluginRepository;
    private ScannerPluginRepository scannerPluginRepository;
    private RulePluginRepository rulePluginRepository;
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
        this.rulePluginRepository.initialize();
        this.analyzerPluginRepository = new AnalyzerPluginRepositoryImpl(pluginConfigurationReader);
        this.analyzerPluginRepository.initialize();
        this.classLoader = pluginConfigurationReader.getClassLoader();
    }

    @Override
    public void destroy() {
        ofNullable(scannerPluginRepository).ifPresent(ScannerPluginRepository::destroy);
        ofNullable(rulePluginRepository).ifPresent(RulePluginRepository::destroy);
        ofNullable(analyzerPluginRepository).ifPresent(AnalyzerPluginRepository::destroy);
        ofNullable(storePluginRepository).ifPresent(StorePluginRepository::destroy);
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
    public AnalyzerPluginRepository getAnalyzerPluginRepository() {
        return analyzerPluginRepository;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Collection<PluginInfo> getPluginOverview() {
        ArrayList<PluginInfo> infos = new ArrayList<>();

        pluginConfigurationReader.getPlugins().forEach(plugin -> {
            String id = plugin.getId();
            String name = plugin.getName();
            PluginInfo info = new PluginInfoImpl(id, name);
            infos.add(info);
        });

        return Collections.unmodifiableCollection(infos);
    }

}
