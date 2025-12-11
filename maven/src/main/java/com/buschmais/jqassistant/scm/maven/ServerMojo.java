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
public class ServerMojo extends AbstractMojo {

    @Override
    protected boolean isConnectorRequired() {
        return true;
    }

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenStoreTask(cachingStoreProvider) {

            @Override
            public void leaveProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
                withStore(store -> server(mavenTaskContext, (EmbeddedGraphStore) store), mavenTaskContext);
            }

            private void server(MavenTaskContext mavenTaskContext, EmbeddedGraphStore store) throws MojoExecutionException {
                MavenProject rootModule = mavenTaskContext.getRootModule();
                getLog().info("Running server for module " + rootModule.getGroupId() + ":" + rootModule.getArtifactId() + ":" + rootModule.getVersion());
                EmbeddedNeo4jServer server = store.getEmbeddedNeo4jServer();
                server.start();
                if (mavenTaskContext.getConfiguration()
                    .server()
                    .openBrowser()) {
                    server.openBrowser();
                }
                if (!mavenTaskContext.getConfiguration()
                    .server()
                    .daemon()) {
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
        };
    }
}
