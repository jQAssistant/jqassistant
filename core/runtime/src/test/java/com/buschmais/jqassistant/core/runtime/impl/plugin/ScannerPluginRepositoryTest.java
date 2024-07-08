package com.buschmais.jqassistant.core.runtime.impl.plugin;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScannerPluginRepositoryTest {

    @Test
    void scopes() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(
            new PluginClassLoader(ScannerPluginRepositoryTest.class.getClassLoader()));
        ScannerPluginRepository repository = new ScannerPluginRepositoryImpl(pluginConfigurationReader);
        assertThat(repository.getScope("test:foo")).isEqualTo(TestScope.FOO);
        assertThat(repository.getScope("Test:foo")).isEqualTo(TestScope.FOO);
        assertThat(repository.getScope("test:Foo")).isEqualTo(TestScope.FOO);
    }
}
