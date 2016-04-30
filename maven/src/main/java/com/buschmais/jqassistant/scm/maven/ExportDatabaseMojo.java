package com.buschmais.jqassistant.scm.maven;

import java.io.*;
import java.util.List;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.neo4j.cypher.export.DatabaseSubGraph;
import org.neo4j.cypher.export.SubGraph;
import org.neo4j.cypher.export.SubGraphExporter;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Exports the database as a file containing cypher statements.
 */
@Mojo(name = "export-database", threadSafe = true)
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
    protected void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        File file = ProjectResolver.getOutputFile(rootModule, exportFile, EXPORT_FILE);
        getLog().info("Exporting database to '" + file.getAbsolutePath() + "'");
        EmbeddedGraphStore graphStore = (EmbeddedGraphStore) store;
        store.beginTransaction();
        try {
            GraphDatabaseService databaseService = graphStore.getGraphDatabaseService();
            SubGraph graph = DatabaseSubGraph.from(databaseService);
            new SubGraphExporter(graph).export(new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot export database.", e);
        } finally {
            store.commitTransaction();
        }
    }

}
