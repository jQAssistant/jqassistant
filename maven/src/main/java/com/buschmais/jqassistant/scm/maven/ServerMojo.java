package com.buschmais.jqassistant.scm.maven;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Starts an embedded Neo4j server.
 */
@Mojo(name = "server", threadSafe = true,
    configurator = "custom")
public class ServerMojo extends AbstractProjectMojo {

    /**
     * The address the server shall bind to.
     */
    @Parameter(property = "jqassistant.server.address", defaultValue = EmbeddedNeo4jServer.DEFAULT_ADDRESS)
    protected String serverAddress;

    /**
     * The port the server shall bind to.
     */
    @Parameter(property = "jqassistant.server.port")
    protected Integer serverPort;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException {
        EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
        EmbeddedNeo4jServer server = embeddedGraphStore.getServer();
        server.start(serverAddress,
            serverPort != null ? serverPort : EmbeddedNeo4jServer.DEFAULT_PORT);
        getLog().info("Running server for module " + rootModule.getGroupId() + ":" + rootModule.getArtifactId() + ":" + rootModule.getVersion());
        getLog().info("Press <Enter> to finish.");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read from System.in.", e);
        } finally {
            server.stop();
        }
    }
}
