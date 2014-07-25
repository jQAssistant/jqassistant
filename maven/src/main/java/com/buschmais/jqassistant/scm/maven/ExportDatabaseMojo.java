package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.neo4j.cypher.export.DatabaseSubGraph;
import org.neo4j.cypher.export.SubGraph;
import org.neo4j.cypher.export.SubGraphExporter;
import org.neo4j.kernel.GraphDatabaseAPI;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * Exports the database as a file containing cypher statements.
 */
@Mojo(name = "export-database")
public class ExportDatabaseMojo extends AbstractProjectMojo {

    private static final String EXPORT_FILE = "jqassistant.cypher";

    /**
     * The file to write the exported cypher statements to.
     */
    @Parameter(property = "jqassistant.export.file")
    protected File exportFile;

    @Override
    protected void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        EmbeddedGraphStore graphStore = (EmbeddedGraphStore) store;
        GraphDatabaseAPI databaseService = graphStore.getDatabaseService();
        File file = BaseProjectResolver.getOutputFile(baseProject, exportFile, EXPORT_FILE);
        getLog().info("Exporting database to '" + file.getAbsolutePath() + "'");
        store.beginTransaction();
        SubGraph graph = DatabaseSubGraph.from(databaseService);
        try {
            new SubGraphExporter(graph).export(new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot export database.", e);
        } finally {
            store.commitTransaction();
        }
    }

}
