package com.buschmais.jqassistant.core.store.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.buschmais.jqassistant.core.store.api.configuration.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.XOTransaction;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@ExtendWith(MockitoExtension.class)
class AbstractGraphStoreTest {

    @Mock
    private Store storeConfiguration;

    @Mock
    private StorePluginRepository storePluginRepository;

    @Mock
    private XOManagerFactory xoManagerFactory;

    @Mock
    private XOManager xoManager;

    @Mock
    private XOTransaction xoTransaction;


    @BeforeEach
    void setUp() {
        doReturn(xoManager).when(xoManagerFactory).createXOManager();
        doReturn(xoTransaction).when(xoManager).currentTransaction();
    }

    @Test
    void defaultAutoCommit() throws URISyntaxException {
        doReturn(empty()).when(storeConfiguration).autoCommitThreshold();
        verifyAutoCommit(10);
    }

    @Test
    void configuredAutoCommit() throws URISyntaxException {
        doReturn(of(40)).when(storeConfiguration).autoCommitThreshold();
        verifyAutoCommit(5);
    }

    private void verifyAutoCommit(int expectedAutoCommits) throws URISyntaxException {
        TestStore testStore = new TestStore(new URI("test:///"), storeConfiguration, storePluginRepository);
        testStore.start();
        for (int i = 0; i < 50; i++) {
            Node node1 = testStore.create(Node.class);
            Node node2 = testStore.create(Node.class);
            Node node3 = testStore.create(Node.class);
            testStore.delete(node3);
            // relations are not considered
            testStore.create(node1, Node2NodeRelation.class, node2);
        }

        verify(xoTransaction, times(expectedAutoCommits)).commit();
        verify(xoTransaction, times(expectedAutoCommits)).begin();
        testStore.stop();
    }

    @Label
    public interface Node extends Descriptor {
    }

    @Relation
    public interface Node2NodeRelation extends Descriptor {
    }

    /**
     * {@link AbstractGraphStore} implementation for testing purposes.
     */
    private class TestStore extends AbstractGraphStore {


        protected TestStore(URI uri, Store configuration, StorePluginRepository storePluginRepository) {
            super(uri, configuration, storePluginRepository);
        }

        @Override
        protected XOManagerFactory<?, ?, ?, ?> getXOManagerFactory(XOUnit xoUnit) {
            return xoManagerFactory;
        }

        @Override
        protected void configure(XOUnit.XOUnitBuilder builder) {
            // nothing to do
        }

        @Override
        protected void initialize(XOManagerFactory xoManagerFactory) {
            // nothing to do
        }

        @Override
        protected void destroy() {
            // nothing to do
        }

        @Override
        protected int getDefaultAutocommitThreshold() {
            return 20;
        }
    }

}
