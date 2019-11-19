package com.buschmais.jqassistant.core.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.ScopePluginRepository;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThat;

public class ScopePluginRepositoryTest {

    @Test
    public void scopes() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        ScopePluginRepository repository = new ScopePluginRepositoryImpl(pluginConfigurationReader);
        assertThat(repository.getScope("test:foo"), Matchers.equalTo(TestScope.FOO));
        assertThat(repository.getScope("Test:foo"), Matchers.equalTo(TestScope.FOO));
        assertThat(repository.getScope("test:Foo"), Matchers.equalTo(TestScope.FOO));
    }
}
