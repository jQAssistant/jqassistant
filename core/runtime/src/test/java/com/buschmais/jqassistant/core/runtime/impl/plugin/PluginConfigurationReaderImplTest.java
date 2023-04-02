package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.google.common.collect.Iterators;
import org.jqassistant.schema.plugin.v1.JqassistantPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PluginConfigurationReaderImplTest {

    @Test
    void twoPluginsWithTheSameIdCanBeLoaded() throws Exception {
        List<URL> urls = Arrays.asList(new URL("file://1"), new URL("file://2"));
        Enumeration<URL> enumerationOfUrls = Iterators.asEnumeration(urls.iterator());

        PluginConfigurationReaderImpl reader = mock(PluginConfigurationReaderImpl.class);
        JqassistantPlugin plugin1 = mock(JqassistantPlugin.class);
        JqassistantPlugin plugin2 = mock(JqassistantPlugin.class);

        doReturn("Plugin A").when(plugin1).getName();
        doReturn("id_snafu").when(plugin1).getId();
        doReturn("Plugin B").when(plugin2).getName();
        doReturn("id_snafu").when(plugin2).getId();

        doReturn(plugin1).doReturn(plugin2).when(reader).readPlugin(Mockito.any(URL.class));
        doReturn(enumerationOfUrls).when(reader).getPluginClassLoaderResources();
        doCallRealMethod().when(reader).getPlugins();

        assertThat(reader.getPlugins()).hasSize(1)
            .containsAnyOf(plugin1, plugin2);
    }

    @Test
    void twoPluginsWithDifferentIdCanBeLoaded() throws Exception {
        List<URL> urls = Arrays.asList(new URL("file://1"), new URL("file://2"));
        Enumeration<URL> enumerationOfUrls = Iterators.asEnumeration(urls.iterator());

        PluginConfigurationReaderImpl reader = mock(PluginConfigurationReaderImpl.class);
        JqassistantPlugin plugin1 = mock(JqassistantPlugin.class);
        JqassistantPlugin plugin2 = mock(JqassistantPlugin.class);

        doReturn("Plugin A").when(plugin1).getName();
        doReturn("plugin_a").when(plugin1).getId();
        doReturn("Plugin B").when(plugin2).getName();
        doReturn("plugin_b").when(plugin2).getId();

        doReturn(plugin1).doReturn(plugin2).when(reader).readPlugin(Mockito.any(URL.class));
        doReturn(enumerationOfUrls).when(reader).getPluginClassLoaderResources();
        doCallRealMethod().when(reader).getPlugins();

        assertThat(reader.getPlugins()).hasSize(2)
                  .containsAnyOf(plugin1, plugin2);
    }
}
