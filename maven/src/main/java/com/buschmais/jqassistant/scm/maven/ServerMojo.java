package com.buschmais.jqassistant.scm.maven;

import java.io.IOException;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Starts an embedded Neo4j server.
 */
@Mojo(name = "server", threadSafe = true)
public class ServerMojo extends AbstractProjectMojo {

    @Override
    protected boolean isConnectorRequired() {
        return true;
    }

    @Override
    protected void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        withStore(store -> server(mojoExecutionContext, (EmbeddedGraphStore) store), mojoExecutionContext);
    }

    private void server(MojoExecutionContext mojoExecutionContext, EmbeddedGraphStore store) throws MojoExecutionException {
        MavenProject rootModule = mojoExecutionContext.getRootModule();
        EmbeddedGraphStore embeddedGraphStore = store;
        EmbeddedNeo4jServer server = embeddedGraphStore.getEmbeddedNeo4jServer();
        server.start();
        getLog().info("Running server for module " + rootModule.getGroupId() + ":" + rootModule.getArtifactId() + ":" + rootModule.getVersion());
        if (!mojoExecutionContext.getConfiguration().server().daemon()) {
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
}
