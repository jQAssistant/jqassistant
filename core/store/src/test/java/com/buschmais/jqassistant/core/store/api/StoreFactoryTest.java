package com.buschmais.jqassistant.core.store.api;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.store.api.configuration.Remote;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.core.store.impl.RemoteGraphStore;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import org.junit.jupiter.api.BeforeEach;
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
    private Remote remote;

    @Mock
    private StorePluginRepository storePluginRepository;

    @Mock
    private ArtifactProvider artifactProvider;

    private StoreFactory storeFactory;

    @BeforeEach
    void setUp() {
        storeFactory = new StoreFactory(storePluginRepository, artifactProvider);
    }

    @Test
    void file() throws URISyntaxException {
        verifyEmbedded("file://target/jqassistant/store");
    }

    @Test
    void memory() throws URISyntaxException {
        verifyEmbedded("memory:///");
    }

    @Test
    void bolt() throws URISyntaxException {
        verifyRemote("bolt://localhost:7687");
    }

    @Test
    void neo4j() throws URISyntaxException {
        verifyRemote("neo4j://localhost:7687");
    }

    @Test
    void neo4js() throws URISyntaxException {
        verifyRemote("neo4j+s://localhost:7687");
    }

    private void verifyEmbedded(String str) throws URISyntaxException {
        verify(of(new URI(str)), EmbeddedGraphStore.class);
    }

    private void verifyRemote(String str) throws URISyntaxException {
        doReturn(remote).when(configuration)
            .remote();
        verify(of(new URI(str)), RemoteGraphStore.class);
    }

    private void verify(Optional<URI> uri, Class<? extends Store> expectedStoreType) {
        doReturn(uri).when(configuration)
            .uri();
        assertThat(storeFactory.getStore(configuration, () -> new File("store"))).isInstanceOf(expectedStoreType);
    }

}
