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

import org.junit.Ignore;
import org.junit.Test;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.ScanTask;

/**
 * Verifies command line scanning.
 */
public class ScanIT extends AbstractCLIIT {

    @Test
    public void files() throws IOException, InterruptedException {
        URL file = getResource(ScanTask.class);
        URL directory = ScanIT.class.getResource("/");
        String[] args = new String[] { "scan", "-f", file.getFile() + "," + directory.getFile() };
        execute(args);
        verifyTypesScanned(getDefaultStoreDirectory(), ScanTask.class, ScanIT.class);
    }

    @Test
    public void urls() throws IOException, InterruptedException {
        URL directory1 = getResource(ScanTask.class);
        URL directory2 = getResource(ScanIT.class);
        String[] args = new String[] { "scan", "-u", directory1 + "," + directory2 };
        execute(args);
        verifyTypesScanned(getDefaultStoreDirectory(), ScanTask.class, ScanIT.class);
    }

    @Test
    public void storeDirectory() throws IOException, InterruptedException {
        URL file = getResource(ScanTask.class);
        String customStoreDirectory = "tmp/customStore";
        String[] args = new String[] { "scan", "-f", file.getFile(), "-s", customStoreDirectory };
        execute(args);
        verifyTypesScanned(new File(getWorkingDirectory(), customStoreDirectory), ScanTask.class);
    }

    @Test
    public void reset() throws IOException, InterruptedException {
        // Scan a file
        URL file1 = getResource(ScanIT.class);
        String[] args1 = new String[] { "scan", "-f", file1.getFile() };
        execute(args1);
        verifyTypesScanned(getDefaultStoreDirectory(), ScanIT.class);
        // Scan a second file using reset
        URL file2 = getResource(ScanTask.class);
        String[] args2 = new String[] { "scan", "-f", file2.getFile(), "-reset" };
        execute(args2);
        verifyTypesScanned(getDefaultStoreDirectory(), ScanTask.class);
        verifyTypesNotScanned(getWorkingDirectory(), ScanIT.class);
        // Scan the first file againg without reset
        execute(args1);
        verifyTypesScanned(getDefaultStoreDirectory(), ScanIT.class);
    }

    @Test
    @Ignore
    public void pluginClassloader() {

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
        store.beginTransaction();
        Map<String, Object> params = new HashMap<>();
        params.put("type", type.getName());
        Result<CompositeRowObject> result = store.executeQuery("match (t:Type) where t.fqn={type} return count(t) as count", params);
        assertThat(result.hasResult(), equalTo(true));
        Long count = result.getSingleResult().get("count", Long.class);
        store.commitTransaction();
        return count.longValue() == 1;
    }

}
