package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.shell.Output;
import org.neo4j.shell.ShellClient;
import org.neo4j.shell.ShellException;
import org.neo4j.shell.ShellServer;
import org.neo4j.shell.impl.RemoteOutput;
import org.neo4j.shell.impl.SameJvmClient;
import org.neo4j.shell.kernel.GraphDatabaseShellServer;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Set;

/**
 * Exports the database as a file containing cypher statements.
 */
@Mojo(name = "export-database")
public class ExportDatabaseMojo extends AbstractAnalysisAggregatorMojo {

    private static final String EXPORT_FILE = "jqassistant.cypher";

    /**
     * {@link org.neo4j.shell.Output} implementation writing to a file.
     */
    private static final class StreamingOutput implements Output {

        private PrintWriter out;

        public StreamingOutput(File file) throws FileNotFoundException, UnsupportedEncodingException {
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        }

        public void print(Serializable object) {
            out.print(object);
        }

        public void println() {
            out.println();
            out.flush();
        }

        public void println(Serializable object) {
            out.println(object);
            out.flush();
        }

        public Appendable append(char ch) {
            this.print(ch);
            return this;
        }

        public Appendable append(CharSequence sequence) {
            this.println(RemoteOutput.asString(sequence));
            return this;
        }

        public Appendable append(CharSequence sequence, int start, int end) {
            this.print(RemoteOutput.asString(sequence).substring(start, end));
            return this;
        }
    }

    /**
     * The file to write the exported cypher statements to.
     */
    @Parameter(property = "jqassistant.export.file")
    protected File exportFile;

    @Override
    protected void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        EmbeddedGraphStore graphStore = (EmbeddedGraphStore) store;
        GraphDatabaseAPI databaseService = graphStore.getDatabaseService();
        ShellServer shellServer;
        try {
            shellServer = new GraphDatabaseShellServer(databaseService);
        } catch (RemoteException e) {
            throw new MojoExecutionException("Cannot create shell server.", e);
        }
        File file = BaseProjectResolver.getOutputFile(baseProject, exportFile, EXPORT_FILE);
        Output output;
        try {
            output = new StreamingOutput(file);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Cannot create export file.", e);
        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException("Encoding not supported.", e);
        }
        ShellClient shellClient;
        try {
            shellClient = new SameJvmClient(Collections.<String, Serializable>singletonMap("quiet", true), shellServer, output);
        } catch (ShellException e) {
            throw new MojoExecutionException("Cannot create shell client.", e);
        }
        getLog().info("Exporting database to '" + file.getAbsolutePath() + "'");
        try {
            shellClient.evaluate("dump");
        } catch (ShellException e) {
            throw new MojoExecutionException("Cannot execute dump command.", e);
        }
    }

    @Override
    protected boolean isResetStoreOnInitialization() {
        return false;
    }

}
