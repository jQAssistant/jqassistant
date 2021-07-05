package com.buschmais.jqassistant.core.store.api;

import java.net.URI;
import java.net.URISyntaxException;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.core.store.impl.RemoteGraphStore;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StoreFactoryTest {

    @Mock
    private StorePluginRepository storePluginRepository;

    @Test
    void file() throws URISyntaxException {
        verify("file://target/jqassistant/store", EmbeddedGraphStore.class);
    }

    @Test
    void memory() throws URISyntaxException {
        verify("memory:///", EmbeddedGraphStore.class);
    }

    @Test
    void bolt() throws URISyntaxException {
        verify("bolt://localhost:7687", RemoteGraphStore.class);
    }

    @Test
    void neo4j() throws URISyntaxException {
        verify("neo4j://localhost:7687", RemoteGraphStore.class);
    }

    @Test
    void neo4js() throws URISyntaxException {
        verify("neo4j+s://localhost:7687", RemoteGraphStore.class);
    }

    private void verify(String uri, Class<? extends Store> expectedStoreType) throws URISyntaxException {
        StoreConfiguration storeConfiguration = StoreConfiguration.builder().uri(new URI(uri)).build();
        Store store = StoreFactory.getStore(storeConfiguration, storePluginRepository);
        assertThat(store).isInstanceOf(expectedStoreType);
    }

}
