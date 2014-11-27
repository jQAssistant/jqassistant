package com.buschmais.jqassistant.plugin.impl;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ScopePluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.ScopePluginRepositoryImpl;
import com.buschmais.jqassistant.core.scanner.api.Scope;

public class ScopePluginRepositoryTest {

    @Test
    public void scopes() throws PluginRepositoryException {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        ScopePluginRepository repository = new ScopePluginRepositoryImpl(pluginConfigurationReader);
        assertThat(repository.getScope("test:foo"), Matchers.<Scope> equalTo(TestScope.FOO));
        assertThat(repository.getScope("Test:foo"), Matchers.<Scope> equalTo(TestScope.FOO));
        assertThat(repository.getScope("test:Foo"), Matchers.<Scope> equalTo(TestScope.FOO));
    }
}
