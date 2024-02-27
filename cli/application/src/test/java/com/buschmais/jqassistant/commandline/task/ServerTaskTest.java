package com.buschmais.jqassistant.commandline.task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.runtime.api.configuration.Server;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerTaskTest {

    @Mock
    private CliConfiguration configuration;

    @Mock
    private Server server;

    @Mock
    private PluginRepository pluginRepository;

    @Mock
    private ArtifactProvider artifactProvider;

    @Mock
    private EmbeddedGraphStore store;

    @Mock
    private EmbeddedNeo4jServer embeddedNeo4jServer;

    private ServerTask serverTask;

    @BeforeEach
    final void setUp() {
        doReturn(embeddedNeo4jServer).when(store)
            .getServer();
        doReturn(server).when(configuration).server();
        serverTask = new ServerTask() {
            @Override
            void withStore(CliConfiguration configuration, StoreOperation storeOperation) throws CliExecutionException {
                storeOperation.run(store);
            }
        };
    }

    @Test
    void daemon() throws CliExecutionException, ParseException {
        doReturn(true).when(server).daemon();

        startServer();

        verify(embeddedNeo4jServer).start();
        verify(embeddedNeo4jServer, never()).stop();
    }

    @Test
    void standalone() throws CliExecutionException, ParseException {
        String data = "\r\n";
        InputStream stdin = System.in;
        try {
            System.setIn(new ByteArrayInputStream(data.getBytes()));
            startServer();
        } finally {
            System.setIn(stdin);

        }

        verify(embeddedNeo4jServer).start();
        verify(embeddedNeo4jServer).stop();
    }

    private void startServer(String... arguments) throws ParseException, CliExecutionException {
        Options options = new Options();
        for (Option option : serverTask.getOptions()) {
            options.addOption(option);
        }
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, arguments);

        serverTask.initialize(pluginRepository, artifactProvider);
        serverTask.configure(commandLine, mock(ConfigurationBuilder.class));
        serverTask.run(configuration, options);
    }

}
