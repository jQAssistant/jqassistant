package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

/**
 * Verifies command line scanning.
 */
public class ScanIT extends AbstractCLIIT {

    private static final String CLASSPATH_SCOPE_SUFFIX = "java:classpath::";

    public ScanIT(String neo4jVersion) {
        super(neo4jVersion);
    }

    @Test
    public void classFromDirectory() throws IOException, InterruptedException {
        String directory = ScanIT.class.getResource("/").getFile();
        String[] args = new String[] { "scan", "-f", CLASSPATH_SCOPE_SUFFIX + directory };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyTypesScanned(getDefaultStoreDirectory(), ScanIT.class);
    }

    @Test
    public void files() throws IOException, InterruptedException {
        URL directory = ScanIT.class.getResource("/");
        String[] args = new String[] { "scan", "-f", directory.getFile() };
        assertThat(execute(args).getExitCode(), equalTo(0));

        EmbeddedGraphStore store = new EmbeddedGraphStore(getDefaultStoreDirectory().getAbsolutePath());
        store.start(Collections.emptyList());
        Map<String, Object> params = new HashMap<>();
        params.put("fileName", "/META-INF");
        String query = "match (f:File:Directory) where f.fileName={fileName} return count(f) as count";
        Long count = executeQuery(store, query, params, "count", Long.class);
        store.stop();
        assertThat(count, equalTo(1l));
    }

    @Test
    public void pluginClassLoader() throws IOException, InterruptedException {
        File testClassDirectory = new File(ScanIT.class.getResource("/").getFile());
        String[] args = new String[] { "scan", "-f", CLASSPATH_SCOPE_SUFFIX + testClassDirectory.getAbsolutePath() };
        assertThat(execute(args).getExitCode(), equalTo(0));
        EmbeddedGraphStore store = new EmbeddedGraphStore(getDefaultStoreDirectory().getAbsolutePath());
        store.start(Collections.<Class<?>> emptyList());
        Long count = executeQuery(store, "match (b:Cdi:Beans) return count(b) as count", Collections.<String, Object> emptyMap(), "count", Long.class);
        assertThat("Expecting one beans.xml descriptor.", count, equalTo(1l));
        store.stop();
    }

    @Test
    public void reset() throws IOException, InterruptedException {
        assumeThat("This test requires Neo4j V2.", neo4jVersion, equalTo(NEO4JV2));
        // Scan a file
        URL file1 = getResource(ScanIT.class);
        String[] args1 = new String[] { "scan", "-f", file1.getFile() };
        assertThat(execute(args1).getExitCode(), equalTo(0));
        // Scan a second file using reset
        URL file2 = getResource(AnalyzeIT.class);
        String[] args2 = new String[] { "scan", "-f", file2.getFile(), "-reset" };
        assertThat(execute(args2).getExitCode(), equalTo(0));
        verifyFilesScanned(getDefaultStoreDirectory(),  new File(file2.getFile()));
        verifyFilesNotScanned(getDefaultStoreDirectory(),  new File(file1.getFile()));
    }

    @Test
    public void storeDirectory() throws IOException, InterruptedException {
        File directory = new File(getWorkingDirectory(), "store1");
        FileUtils.deleteDirectory(directory);
        URL file = getResource(ScanIT.class);
        String[] args2 = new String[] { "scan", "-f", file.getFile(), "-s", directory.getAbsolutePath()};
        assertThat(execute(args2).getExitCode(), equalTo(0));
        verifyFilesScanned(directory,  new File(file.getFile()));
    }

    @Test
    public void storeUri() throws IOException, InterruptedException {
        File directory = new File(getWorkingDirectory(), "store2");
        FileUtils.deleteDirectory(directory);
        URL file = getResource(ScanIT.class);
        String[] args2 = new String[] { "scan", "-f", file.getFile(), "-storeUri", directory.toURI().toString() };
        assertThat(execute(args2).getExitCode(), equalTo(0));
        verifyFilesScanned(directory,  new File(file.getFile()));
    }

    /**
     * Verify that it's not allowed to specify both storeDirectory and storeUri.
     *
     * @throws IOException
     *             If the test fails.
     * @throws InterruptedException
     *             If execution is interrupted.
     */
    @Test
    public void storeUriAndDirectory() throws IOException, InterruptedException {
        File directory = new File(getWorkingDirectory(), "store1");
        FileUtils.deleteDirectory(directory);
        URL file = getResource(ScanIT.class);
        String[] args2 = new String[] { "scan", "-f", file.getFile(), "-s", directory.getAbsolutePath(), "-storeUri",
                directory.toURI().toString() };
        assertThat(execute(args2).getExitCode(), equalTo(1));
        verifyFilesNotScanned(directory, new File(file.getFile()));
    }

    /**
     * Converts a class to a URL.
     *
     * @param type
     *            The class.
     * @return The URL.
     */
    private URL getResource(Class<?> type) {
        return type.getResource("/" + type.getName().replace(".", "/") + ".class");
    }

    /**
     * Executes a query with single result and returns it.
     *
     * @param store
     *            The initialized store.
     * @param query
     *            The query.
     * @param params
     *            The parameters.
     * @return The result.
     */

    private <T> T executeQuery(EmbeddedGraphStore store, String query, Map<String, Object> params, String resultColumn, Class<T> resultType) {
        store.beginTransaction();
        Result<CompositeRowObject> result = store.executeQuery(query, params);
        assertThat(result.hasResult(), equalTo(true));
        T value = result.getSingleResult().get(resultColumn, resultType);
        store.commitTransaction();
        return value;
    }

    /**
     * Determine if a specific type is in the database.
     *
     * @param store
     *            The store
     * @param type
     *            The type
     * @return <code>true</code> if the type is represented in the database.
     */
    private boolean isTypeScanned(EmbeddedGraphStore store, Class<?> type) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type.getName());
        String query = "match (t:Type:Class) where t.fqn={type} return count(t) as count";
        Long count = executeQuery(store, query, params, "count", Long.class);
        return count.longValue() == 1;
    }

    /**
     * Determine if a specific file is in the database.
     *
     * @param store
     *            The store
     * @param file
     *            The file
     * @return <code>true</code> if the file is represented in the database.
     */
    private boolean isFileScanned(EmbeddedGraphStore store, File file) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", file.getAbsolutePath().replace("\\", "/"));
        String query = "match (t:File) where t.fileName={name} return count(t) as count";
        Long count = executeQuery(store, query, params, "count", Long.class);
        return count.longValue() == 1;
    }

    /**
     * Verifies if a database is created not containing the the given files.
     *
     * @param directory
     *            The database directory.
     * @param files
     *            The types.
     */
    private void verifyFilesNotScanned(File directory, File... files) {
        EmbeddedGraphStore store = new EmbeddedGraphStore(directory.getAbsolutePath());
        store.start(Collections.<Class<?>> emptyList());
        for (File file : files) {
            assertThat("Expecting no result for " + file, isFileScanned(store, file), equalTo(false));
        }
        store.stop();
    }

    /**
     * Verifies if a database is created containing the the given types.
     *
     * @param directory
     *            The database directory.
     * @param types
     *            The types.
     */
    private void verifyTypesScanned(File directory, Class<?>... types) {
        EmbeddedGraphStore store = new EmbeddedGraphStore(directory.getAbsolutePath());
        store.start(Collections.<Class<?>> emptyList());
        for (Class<?> type : types) {
            assertThat("Expecting a result for " + type.getName(), isTypeScanned(store, type), equalTo(true));
        }
        store.stop();
    }

    /**
     * Verifies if a database is created containing the the given types.
     *
     * @param directory
     *            The database directory.
     * @param files
     *            The files.
     */
    private void verifyFilesScanned(File directory, File... files) {
        EmbeddedGraphStore store = new EmbeddedGraphStore(directory.getAbsolutePath());
        store.start(Collections.<Class<?>> emptyList());
        for (File file : files) {
            assertThat("Expecting a result for " + file, isFileScanned(store, file), equalTo(true));
        }
        store.stop();
    }
}
