package com.buschmais.jqassistant.core.plugin.impl;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;

import com.google.common.collect.Iterators;
import org.assertj.core.api.Assertions;
import org.jqassistant.schema.plugin.v1.JqassistantPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PluginConfigurationReaderImplTest {

    @Test
    void twoPluginsWithTheSameIdCausesAnPluginRepositoryException() throws Exception {
        List<URL> urls = Arrays.asList(new URL("file://1"), new URL("file://2"));
        Enumeration<URL> enumerationOfUrls = Iterators.asEnumeration(urls.iterator());

        PluginConfigurationReaderImpl reader = Mockito.mock(PluginConfigurationReaderImpl.class);
        JqassistantPlugin plugin1 = Mockito.mock(JqassistantPlugin.class);
        JqassistantPlugin plugin2 = Mockito.mock(JqassistantPlugin.class);

        Mockito.doReturn("Plugin A").when(plugin1).getName();
        Mockito.doReturn("id_snafu").when(plugin1).getId();
        Mockito.doReturn("Plugin B").when(plugin2).getName();
        Mockito.doReturn("id_snafu").when(plugin2).getId();

        Mockito.doReturn(plugin1).doReturn(plugin2).when(reader).readPlugin(Mockito.any(URL.class));
        Mockito.doReturn(enumerationOfUrls).when(reader).getPluginClassLoaderResources();
        Mockito.doCallRealMethod().when(reader).getPlugins();

        Assertions.assertThatThrownBy(reader::getPlugins)
                  .isInstanceOf(PluginRepositoryException.class);
    }

    @Test
    void twoPluginsWithDifferentIdCanBeLoaded() throws Exception {
        List<URL> urls = Arrays.asList(new URL("file://1"), new URL("file://2"));
        Enumeration<URL> enumerationOfUrls = Iterators.asEnumeration(urls.iterator());

        PluginConfigurationReaderImpl reader = Mockito.mock(PluginConfigurationReaderImpl.class);
        JqassistantPlugin plugin1 = Mockito.mock(JqassistantPlugin.class);
        JqassistantPlugin plugin2 = Mockito.mock(JqassistantPlugin.class);

        Mockito.doReturn("Plugin A").when(plugin1).getName();
        Mockito.doReturn("plugin_a").when(plugin1).getId();
        Mockito.doReturn("Plugin B").when(plugin2).getName();
        Mockito.doReturn("plugin_b").when(plugin2).getId();

        Mockito.doReturn(plugin1).doReturn(plugin2).when(reader).readPlugin(Mockito.any(URL.class));
        Mockito.doReturn(enumerationOfUrls).when(reader).getPluginClassLoaderResources();
        Mockito.doCallRealMethod().when(reader).getPlugins();

        Assertions.assertThat(reader.getPlugins()).hasSize(2)
                  .containsAnyOf(plugin1, plugin2);
    }
}
