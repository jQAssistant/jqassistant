package com.buschmais.jqassistant.core.store.api;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.core.store.impl.RemoteGraphStore;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class StoreFactoryTest {

    @Mock
    private com.buschmais.jqassistant.core.store.api.configuration.Store configuration;

    @Mock
    private StorePluginRepository storePluginRepository;

    @Test
    void file() throws URISyntaxException {
        verify(of(new URI("file://target/jqassistant/store")), EmbeddedGraphStore.class);
    }

    @Test
    void memory() throws URISyntaxException {
        verify(of(new URI("memory:///")), EmbeddedGraphStore.class);
    }

    @Test
    void bolt() throws URISyntaxException {
        verify(of(new URI("bolt://localhost:7687")), RemoteGraphStore.class);
    }

    @Test
    void neo4j() throws URISyntaxException {
        verify(of(new URI("neo4j://localhost:7687")), RemoteGraphStore.class);
    }

    @Test
    void neo4js() throws URISyntaxException {
        verify(of(new URI("neo4j+s://localhost:7687")), RemoteGraphStore.class);
    }

    private void verify(Optional<URI> uri, Class<? extends Store> expectedStoreType) {
        doReturn(uri).when(configuration)
            .uri();
        assertThat(StoreFactory.getStore(configuration, () -> new File("store"), storePluginRepository)).isInstanceOf(expectedStoreType);
    }

}
