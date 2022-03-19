package com.buschmais.jqassistant.scm.maven;

import java.io.*;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jDatastoreSession;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.neo4j.cypher.export.DatabaseSubGraph;
import org.neo4j.cypher.export.SubGraph;
import org.neo4j.cypher.export.SubGraphExporter;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Exports the database as a file containing cypher statements.
 */
@Mojo(name = "export-database", threadSafe = true, configurator = "custom")
public class ExportDatabaseMojo extends AbstractProjectMojo {

    private static final String EXPORT_FILE = "jqassistant.cypher";

    /**
     * The file to write the exported cypher statements to.
     */
    @Parameter(property = "jqassistant.export.file")
    protected File exportFile;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    protected void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        withStore(store -> export(store, mojoExecutionContext.getOutputFile(exportFile, EXPORT_FILE)), mojoExecutionContext);
    }

    private void export(Store store, File file) throws MojoExecutionException {
        getLog().info("Exporting database to '" + file.getAbsolutePath() + "'");
        store.beginTransaction();
        try {
            GraphDatabaseService graphDatabaseService = store.getXOManager()
                .getDatastoreSession(EmbeddedNeo4jDatastoreSession.class)
                .getGraphDatabaseService();
            SubGraph graph = DatabaseSubGraph.from(graphDatabaseService);
            new SubGraphExporter(graph).export(new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot export database.", e);
        } finally {
            store.commitTransaction();
        }
    }

}
