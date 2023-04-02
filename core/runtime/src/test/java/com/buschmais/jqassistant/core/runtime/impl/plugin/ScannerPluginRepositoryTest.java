package com.buschmais.jqassistant.core.runtime.impl.plugin;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class ScannerPluginRepositoryTest {

    @Test
    void scopes() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(
            new PluginClassLoader(ScannerPluginRepositoryTest.class.getClassLoader()));
        ScannerPluginRepository repository = new ScannerPluginRepositoryImpl(pluginConfigurationReader);
        assertThat(repository.getScope("test:foo"), Matchers.equalTo(TestScope.FOO));
        assertThat(repository.getScope("Test:foo"), Matchers.equalTo(TestScope.FOO));
        assertThat(repository.getScope("test:Foo"), Matchers.equalTo(TestScope.FOO));
    }
}
