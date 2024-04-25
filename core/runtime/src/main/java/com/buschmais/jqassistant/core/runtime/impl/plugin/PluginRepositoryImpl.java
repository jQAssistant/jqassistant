package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginInfo;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import lombok.extern.slf4j.Slf4j;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * The plugin repository.
 */
@Slf4j
public class PluginRepositoryImpl implements PluginRepository {

    private final PluginConfigurationReader pluginConfigurationReader;

    private StorePluginRepository storePluginRepository;
    private ScannerPluginRepository scannerPluginRepository;
    private RulePluginRepository rulePluginRepository;
    private AnalyzerPluginRepository analyzerPluginRepository;

    private ClassLoader classLoader;

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     *     The plugin configuration reader.
     */
    public PluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        this.pluginConfigurationReader = pluginConfigurationReader;
        this.printPluginInfos();
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
    public List<PluginInfo> getPluginInfos() {
        return pluginConfigurationReader.getPlugins()
            .stream()
            .map(plugin -> PluginInfoImpl.builder()
                .id(plugin.getId())
                .name(plugin.getName())
                .version(ofNullable(plugin.getVersion()))
                .build())
            .sorted(PluginInfo.ID_COMPARATOR)
            .collect(toList());
    }

    @Override
    public void printPluginInfos() {
        for (PluginInfo pluginInfo : getPluginInfos()) {
            log.info("{} {} [{}]", pluginInfo.getName(), pluginInfo.getVersion()
                .orElse("<unknown version>"), pluginInfo.getId());
        }
    }
}
