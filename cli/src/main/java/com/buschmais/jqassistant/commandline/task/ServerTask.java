package com.buschmais.jqassistant.commandline.task;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.bootstrap.impl.EmbeddedNeoServer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ServerTask extends AbstractTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTask.class);

    public static final String CMDLINE_OPTION_SERVERADDRESS = "serverAddress";
    public static final String CMDLINE_OPTION_SERVERPORT = "serverPort";

    private String serverAddress;
    private int serverPort;

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
        EmbeddedNeoServer server = new EmbeddedNeoServer();
        server.init(embeddedGraphStore.getGraphDatabaseService());
        server.start(serverAddress, serverPort);
        LOGGER.info("Running server");
        LOGGER.info("Press <Enter> to finish.");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new CliExecutionException("Cannot read from console.", e);
        } finally {
            server.stop();
        }
    }

    @Override
    protected void addTaskOptions(final List<Option> options) {
        super.addTaskOptions(options);
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_SERVERADDRESS).withDescription("The binding address of the server.").hasArgs()
                .create(CMDLINE_OPTION_SERVERADDRESS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_SERVERPORT).withDescription("The binding port of the server.").hasArgs()
                .create(CMDLINE_OPTION_SERVERPORT));
    }

    @Override
    public void withOptions(CommandLine options) {
        serverAddress = getOptionValue(options, CMDLINE_OPTION_SERVERADDRESS, EmbeddedNeoServer.DEFAULT_ADDRESS);
        serverPort = Integer.valueOf(getOptionValue(options, CMDLINE_OPTION_SERVERPORT, Integer.toString(EmbeddedNeoServer.DEFAULT_PORT)));
    }
}
