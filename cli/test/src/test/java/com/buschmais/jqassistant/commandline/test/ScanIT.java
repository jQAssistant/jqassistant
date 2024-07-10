package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import org.apache.commons.io.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line scanning.
 */
class ScanIT extends AbstractCLIIT {

    private static final String CLASSPATH_SCOPE_SUFFIX = "java:classpath::";

    @DistributionTest
    void classFromDirectory() {
        String directory = ScanIT.class.getResource("/")
            .getFile();
        String[] args = new String[] { "scan", "-f", CLASSPATH_SCOPE_SUFFIX + directory };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(store -> verifyTypesScanned(store, ScanIT.class));
    }

    @DistributionTest
    void files() {
        URL directory = ScanIT.class.getResource("/");
        String[] args = new String[] { "scan", "-f", directory.getFile() };
        assertThat(execute(args).getExitCode()).isZero();

        withStore(store -> {
            Map<String, Object> params = new HashMap<>();
            params.put("fileName", "/META-INF");
            String query = "match (f:File:Directory) where f.fileName=$fileName return count(f) as count";
            Long count = executeQuery(store, query, params, "count", Long.class);
            assertThat(count).isEqualTo(1L);
        });
    }

    @DistributionTest
    void filesFromConfigFile() {
        File configFile = new File(ScanIT.class.getResource("/.jqassistant-with-scan-include.yml")
            .getFile());
        String[] args = new String[] { "scan", "-configurationLocations", configFile.getAbsolutePath() };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(store -> {
            Map<String, Object> params = new HashMap<>();
            params.put("fileName", "/META-INF");
            String query = "match (f:File:Directory) where f.fileName=$fileName return count(f) as count";
            Long count = executeQuery(store, query, params, "count", Long.class);
            assertThat(count).isEqualTo(1L);
        });
    }

    @DistributionTest
    void customMavenSettings() throws InterruptedException {
        File customRepository = new File(getWorkingDirectory(), "custom-repository/");
        File mavenSettings = new File(ScanIT.class.getResource("/userhome/custom-maven-settings.xml")
            .getFile());

        execute("scan", "-mavenSettings", mavenSettings.getAbsolutePath()).getProcess()
            .waitFor();

        assertThat(customRepository).exists();
    }

    @DistributionTest
    void reset() {
        URL file = getResource(AnalyzeIT.class);
        String[] args = new String[] { "scan", "-f", file.getFile(), "-D", "jqassistant.scan.reset=true" };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("Resetting store"));
        withStore(store -> verifyFilesScanned(store, new File(file.getFile())));
    }

    @DistributionTest
    void storeUri() throws IOException {
        File directory = new File(getWorkingDirectory(), "store2");
        FileUtils.deleteDirectory(directory);
        URL file = getResource(ScanIT.class);
        String[] args = new String[] { "scan", "-f", file.getFile(), "-D", "jqassistant.store.uri=" + directory.toURI() };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(directory, store -> verifyFilesScanned(store, new File(file.getFile())));
    }

    /**
     * Converts a class to a URL.
     *
     * @param type
     *     The class.
     * @return The URL.
     */
    private URL getResource(Class<?> type) {
        return type.getResource("/" + type.getName()
            .replace(".", "/") + ".class");
    }

    /**
     * Executes a query with single result and returns it.
     *
     * @param store
     *     The initialized store.
     * @param query
     *     The query.
     * @param params
     *     The parameters.
     * @return The result.
     */

    private <T> T executeQuery(Store store, String query, Map<String, Object> params, String resultColumn, Class<T> resultType) {
        store.beginTransaction();
        Result<CompositeRowObject> result = store.executeQuery(query, params);
        assertThat(result.hasResult()).isTrue();
        T value = result.getSingleResult()
            .get(resultColumn, resultType);
        store.commitTransaction();
        return value;
    }

    /**
     * Determine if a specific type is in the database.
     *
     * @param store
     *     The store
     * @param type
     *     The type
     * @return <code>true</code> if the type is represented in the database.
     */
    private boolean isTypeScanned(Store store, Class<?> type) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type.getName());
        String query = "match (t:Type:Class) where t.fqn=$type return count(t) as count";
        Long count = executeQuery(store, query, params, "count", Long.class);
        return count == 1;
    }

    /**
     * Determine if a specific file is in the database.
     *
     * @param store
     *     The store
     * @param file
     *     The file
     * @return <code>true</code> if the file is represented in the database.
     */
    private boolean isFileScanned(Store store, File file) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", file.getAbsolutePath()
            .replace("\\", "/"));
        String query = "match (t:File) where t.fileName=$name return count(t) as count";
        Long count = executeQuery(store, query, params, "count", Long.class);
        return count == 1;
    }

    /**
     * Verifies if a database is created not containing the the given files.
     *
     * @param store
     *     The {@link Store}.
     * @param files
     *     The types.
     */
    private void verifyFilesNotScanned(Store store, File... files) {
        for (File file : files) {
            assertThat(isFileScanned(store, file)).describedAs("Expecting no result for %s", file)
                .isFalse();
        }
    }

    /**
     * Verifies if a database is created containing the the given types.
     *
     * @param store
     *     The {@link Store}.
     * @param types
     *     The types.
     */
    private void verifyTypesScanned(Store store, Class<?>... types) {
        for (Class<?> type : types) {
            assertThat(isTypeScanned(store, type)).describedAs("Expecting a result for %s", type.getName())
                .isTrue();
        }
    }

    /**
     * Verifies if a database is created containing the the given types.
     *
     * @param store
     *     The {@link Store}.
     * @param files
     *     The files.
     */
    private void verifyFilesScanned(Store store, File... files) {
        for (File file : files) {
            assertThat(isFileScanned(store, file)).describedAs("Expecting a result for %s", file)
                .isTrue();
        }
    }
}
