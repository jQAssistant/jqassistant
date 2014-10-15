package com.buschmais.jqassistant.scm.cli.test;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.JQATask;
import com.buschmais.jqassistant.scm.cli.Main;
import com.buschmais.jqassistant.scm.cli.ScanTask;

/**
 * Verifies command line scanning.
 */
public class ScanIT extends AbstractCLIIT {

    @Before
    public void before() {
        EmbeddedGraphStore store = new EmbeddedGraphStore(JQATask.DEFAULT_STORE_DIRECTORY);
        store.start(Collections.<Class<?>> emptyList());
        store.reset();
        store.stop();
    }

    @Test
    public void files() throws IOException {
        URL file = getResource(ScanTask.class);
        URL directory = ScanIT.class.getResource("/");
        String[] args = new String[] { "scan", "-f", file.getFile() + "," + directory.getFile() };
        Main.main(args);
        verifyTypesScanned(JQATask.DEFAULT_STORE_DIRECTORY, ScanTask.class, ScanIT.class);
    }

    @Test
    public void urls() throws IOException {
        URL directory1 = getResource(ScanTask.class);
        URL directory2 = getResource(ScanIT.class);
        String[] args = new String[] { "scan", "-u", directory1 + "," + directory2 };
        Main.main(args);
        verifyTypesScanned(JQATask.DEFAULT_STORE_DIRECTORY, ScanTask.class, ScanIT.class);
    }

    @Test
    public void storeDirectory() throws IOException {
        URL file = getResource(ScanTask.class);
        String customStoreDirectory = "tmp/customStore";
        String[] args = new String[] { "scan", "-f", file.getFile(), "-s", customStoreDirectory };
        Main.main(args);
        verifyTypesScanned(customStoreDirectory, ScanTask.class);
    }

    @Test
    public void reset() throws IOException {
        URL file1 = getResource(ScanIT.class);
        String[] args1 = new String[] { "scan", "-f", file1.getFile() };
        Main.main(args1);
        verifyTypesScanned(JQATask.DEFAULT_STORE_DIRECTORY, ScanIT.class);
        URL file2 = getResource(ScanTask.class);
        String[] args2 = new String[] { "scan", "-f", file2.getFile(), "-reset" };
        Main.main(args2);
        verifyTypesScanned(JQATask.DEFAULT_STORE_DIRECTORY, ScanTask.class);
        verifyTypesNotScanned(JQATask.DEFAULT_STORE_DIRECTORY, ScanIT.class);
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
    private void verifyTypesScanned(String directory, Class<?>... types) {
        EmbeddedGraphStore store = new EmbeddedGraphStore(directory);
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
    private void verifyTypesNotScanned(String directory, Class<?>... types) {
        EmbeddedGraphStore store = new EmbeddedGraphStore(directory);
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
