package com.buschmais.jqassistant.scm.maven;

import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import com.buschmais.jqassistant.scm.neo4jserver.impl.DefaultServerImpl;

/**
 * Starts an embedded Neo4j server.
 */
@Mojo(name = "server", threadSafe = true)
public class ServerMojo extends AbstractProjectMojo {

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        Server server = new DefaultServerImpl((EmbeddedGraphStore) store, pluginRepositoryProvider.getScannerPluginRepository(),
                pluginRepositoryProvider.getRulePluginRepository());
        server.start();
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
