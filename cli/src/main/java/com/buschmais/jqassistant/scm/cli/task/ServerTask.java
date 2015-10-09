package com.buschmais.jqassistant.scm.cli.task;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import com.buschmais.jqassistant.scm.neo4jserver.impl.ExtendedCommunityNeoServer;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ServerTask extends AbstractTask {

    public static final String CMDLINE_OPTION_SERVERADDRESS = "serverAddress";
    public static final String CMDLINE_OPTION_SERVERPORT = "serverPort";

    private String serverAddress;
    private int serverPort;

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        Server server;
        try {
            server = new ExtendedCommunityNeoServer((EmbeddedGraphStore) store, pluginRepository.getScannerPluginRepository(), pluginRepository
                    .getRulePluginRepository(), serverAddress, serverPort);
        } catch (PluginRepositoryException e) {
            throw new CliExecutionException("Cannot get plugins.", e);
        }
        server.start();
        getLog().info("Running server");
        getLog().info("Press <Enter> to finish.");
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
        serverAddress = getOptionValue(options, CMDLINE_OPTION_SERVERADDRESS, ExtendedCommunityNeoServer.DEFAULT_ADDRESS);
        serverPort = Integer.valueOf(getOptionValue(options, CMDLINE_OPTION_SERVERPORT, Integer.toString(ExtendedCommunityNeoServer.DEFAULT_PORT)));
    }
}
