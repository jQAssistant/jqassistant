package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.commandline.Task;
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
        withStore(store -> {
            Map<String, Object> params = new HashMap<>();
            params.put("type", ScanIT.class.getName());
            String query = "match (t:Type:Class) where t.fqn=$type return count(t) as count";
            Long count = executeQuery(store, query, params, "count", Long.class);
            assertThat(count).describedAs("Expecting a result for %s", ScanIT.class)
                .isEqualTo(1);
        });
    }

    @DistributionTest
    void filesWithDefaultProjectDirectory() {
        URL directory = ScanIT.class.getResource("/");
        String[] args = new String[] { "scan", "-f", directory.getFile() };
        assertThat(execute(args).getExitCode()).isZero();

        withStore(store -> {
            String query = "match (f:File:Directory) where f.fileName=$fileName and f.path=$path return count(f) as count";
            Long count = executeQuery(store, query, Map.of("fileName", "/META-INF", "path", "../test-classes/META-INF"), "count", Long.class);
            assertThat(count).isEqualTo(1L);
        });
    }

    @DistributionTest
    void filesWithCustomProjectDirectory() {
        String directory = ScanIT.class.getResource("/")
            .getFile();
        File projectDirectory = new File(getWorkingDirectory(), "project");
        String[] args = new String[] { "--projectDirectory", projectDirectory.getAbsolutePath(), "scan", "-f", new File(directory).getAbsolutePath() };
        assertThat(execute(args).getExitCode()).isZero();

        withStore(new File(projectDirectory, Task.DEFAULT_STORE_DIRECTORY), store -> {
            String query = "match (f:File:Directory) where f.fileName=$fileName and f.path=$path return count(f) as count";
            Long count = executeQuery(store, query, Map.of("fileName", "/META-INF", "path", "../../test-classes/META-INF"), "count", Long.class);
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
        URL file = getResource(ScanIT.class);
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
        try (Result<CompositeRowObject> result = store.executeQuery(query, params)) {
            assertThat(result.hasResult()).isTrue();
            return result.getSingleResult()
                .get(resultColumn, resultType);
        } finally {
            store.commitTransaction();
        }
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
        Path relativePath = getWorkingDirectory().toPath()
            .toAbsolutePath()
            .normalize()
            .relativize(file.toPath()
                .toAbsolutePath()
                .normalize());
        Map<String, Object> params = new HashMap<>();
        params.put("name", "/" + relativePath.toString()
            .replace("\\", "/"));
        String query = "match (t:File) where t.fileName=$name return count(t) as count";
        Long count = executeQuery(store, query, params, "count", Long.class);
        return count == 1;
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
