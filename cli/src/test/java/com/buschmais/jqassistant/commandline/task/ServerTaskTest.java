package com.buschmais.jqassistant.commandline.task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;

import org.apache.commons.cli.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServerTaskTest {

    @Mock
    PluginRepository pluginRepository;

    @Mock
    private EmbeddedGraphStore store;

    @Mock
    private EmbeddedNeo4jServer server;

    @Before
    public final void setUp() {
        doReturn(server).when(store).getServer();
    }

    private ServerTask serverTask = new ServerTask();

    @Test
    public void daemon() throws CliExecutionException, ParseException {
        startServer("-daemon");

        verify(server).start(EmbeddedNeo4jServer.DEFAULT_ADDRESS, EmbeddedNeo4jServer.DEFAULT_PORT);
    }

    @Test
    public void standalone() throws CliExecutionException, ParseException {
        String data = "\r\n";
        InputStream stdin = System.in;
        try {
            System.setIn(new ByteArrayInputStream(data.getBytes()));
            startServer(new String[] {});
        } finally {
            System.setIn(stdin);

        }

        verify(server).start(EmbeddedNeo4jServer.DEFAULT_ADDRESS, EmbeddedNeo4jServer.DEFAULT_PORT);
        verify(server).stop();
    }

    private void startServer(String... arguments) throws ParseException, CliExecutionException {
        Options options = new Options();
        for (Option option : serverTask.getOptions()) {
            options.addOption(option);
        }
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, arguments);

        serverTask.initialize(pluginRepository, new HashMap<>());
        serverTask.withOptions(commandLine);
        serverTask.executeTask(store);
    }

}
