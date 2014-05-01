package com.buschmais.jqassistant.scm.maven;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Collections;
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
import org.neo4j.shell.Output;
import org.neo4j.shell.ShellClient;
import org.neo4j.shell.ShellException;
import org.neo4j.shell.ShellServer;
import org.neo4j.shell.impl.RemoteOutput;
import org.neo4j.shell.impl.SameJvmClient;
import org.neo4j.shell.kernel.GraphDatabaseShellServer;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import org.neo4j.shell.kernel.apps.cypher.Exporter;

/**
 * Exports the database as a file containing cypher statements.
 */
@Mojo(name = "export-database")
public class ExportDatabaseMojo extends AbstractAnalysisAggregatorMojo {

    private static final String EXPORT_FILE = "jqassistant.cypher";

    private static final String lineSeparator = System.getProperty("line.separator");

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
            new SubGraphExporter(graph).export(new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
                @Override
                public void println() {
                    print(";");
                    print(lineSeparator);
                    flush();
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot export database.", e);
        } finally {
            store.commitTransaction();
        }
    }

    @Override
    protected boolean isResetStoreOnInitialization() {
        return false;
    }

}
