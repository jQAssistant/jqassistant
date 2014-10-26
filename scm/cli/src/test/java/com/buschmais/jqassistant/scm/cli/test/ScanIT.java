package com.buschmais.jqassistant.scm.cli.test;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.task.ScanTask;

/**
 * Verifies command line scanning.
 */
public class ScanIT extends AbstractCLIIT {

    @Test
    public void files() throws IOException, InterruptedException {
        URL file = getResource(ScanTask.class);
        URL directory = ScanIT.class.getResource("/");
        String[] args = new String[] { "scan", "-f", file.getFile() + "," + directory.getFile() };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyTypesScanned(getDefaultStoreDirectory(), ScanTask.class, ScanIT.class);
    }

    @Test
    public void urls() throws IOException, InterruptedException {
        URL directory1 = getResource(ScanTask.class);
        URL directory2 = getResource(ScanIT.class);
        String[] args = new String[] { "scan", "-u", directory1 + "," + directory2 };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyTypesScanned(getDefaultStoreDirectory(), ScanTask.class, ScanIT.class);
    }

    @Test
    public void storeDirectory() throws IOException, InterruptedException {
        URL file = getResource(ScanTask.class);
        String customStoreDirectory = "tmp/customStore";
        String[] args = new String[] { "scan", "-f", file.getFile(), "-s", customStoreDirectory };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyTypesScanned(new File(getWorkingDirectory(), customStoreDirectory), ScanTask.class);
    }

    @Test
    public void reset() throws IOException, InterruptedException {
        // Scan a file
        URL file1 = getResource(ScanIT.class);
        String[] args1 = new String[] { "scan", "-f", file1.getFile() };
        assertThat(execute(args1).getExitCode(), equalTo(0));
        verifyTypesScanned(getDefaultStoreDirectory(), ScanIT.class);
        // Scan a second file using reset
        URL file2 = getResource(ScanTask.class);
        String[] args2 = new String[] { "scan", "-f", file2.getFile(), "-reset" };
        assertThat(execute(args2).getExitCode(), equalTo(0));
        verifyTypesScanned(getDefaultStoreDirectory(), ScanTask.class);
        verifyTypesNotScanned(getWorkingDirectory(), ScanIT.class);
        // Scan the first file againg without reset
        assertThat(execute(args1).getExitCode(), equalTo(0));
        verifyTypesScanned(getDefaultStoreDirectory(), ScanIT.class);
    }

    @Test
    public void pluginClassloader() throws IOException, InterruptedException {
        File testClassDirectory = new File(ScanIT.class.getResource("/").getFile());
        String[] args = new String[] { "scan", "-f", testClassDirectory.getAbsolutePath() };
        assertThat(execute(args).getExitCode(), equalTo(0));
        EmbeddedGraphStore store = new EmbeddedGraphStore(getDefaultStoreDirectory().getAbsolutePath());
        store.start(Collections.<Class<?>> emptyList());
        Long count = executeQuery(store, "match (b:Cdi:Beans) return count(b) as count", Collections.<String, Object> emptyMap(), "count", Long.class);
        assertThat("Expecting on beans.xml descriptor.", count, equalTo(1l));
        store.stop();
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
     * @param types
     *            The types.
     */
    private void verifyTypesNotScanned(File directory, Class<?>... types) {
        EmbeddedGraphStore store = new EmbeddedGraphStore(directory.getAbsolutePath());
        store.start(Collections.<Class<?>> emptyList());
        for (Class<?> type : types) {
            assertThat("Expecting no result for " + type.getName(), isTypeScanned(store, type), equalTo(false));
        }
        store.stop();
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
        String query = "match (t:Type) where t.fqn={type} return count(t) as count";
        Long count = executeQuery(store, query, params, "count", Long.class);
        return count.longValue() == 1;
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

}
