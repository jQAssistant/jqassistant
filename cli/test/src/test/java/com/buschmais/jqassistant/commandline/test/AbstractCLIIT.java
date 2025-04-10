package com.buschmais.jqassistant.commandline.test;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.report.api.configuration.Build;
import com.buschmais.jqassistant.core.resolver.api.ArtifactProviderFactory;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreFactory;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.Assume.assumeTrue;

/**
 * Abstract base implementation for CLI tests.
 */
@Slf4j
@ExtendWith(AbstractCLIIT.DistributionParameterResolver.class)
public abstract class AbstractCLIIT {

    @RequiredArgsConstructor
    private enum DISTRIBUTION {

        NEO4JV4(Runtime.Version.parse("11"), Runtime.Version.parse("17")),
        NEO4JV5(Runtime.Version.parse("17"), Runtime.Version.parse("21"));

        private final Runtime.Version minRuntimeVersion;
        private final Runtime.Version maxRuntimeVersion;
    }

    /**
     * Resolves the distribution parameter of the method {@link AbstractCLIIT#before(DISTRIBUTION)}}.
     */
    static class DistributionParameterResolver implements ParameterResolver {

        private int distributionIndex = 0;

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getIndex() == 0 && parameterContext.getParameter()
                .getType() == DISTRIBUTION.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            DISTRIBUTION distribution = parameterContext.getIndex() == 0 ? DISTRIBUTION.values()[distributionIndex] : null;
            // increment index per execution and reset after # of distributions is reached (current repetition per method is not available via API)
            distributionIndex = ++distributionIndex % DISTRIBUTION.values().length;
            return distribution;
        }
    }

    /**
     * Meta-annotation for test to executed for multiple distributions
     */
    @RepeatedTest(value = 2) // Must match the length of #DistributionParameterResolver.DISTRIBUTIONS
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface DistributionTest {
    }

    public static final String RULES_DIRECTORY = AbstractCLIIT.class.getResource("/rules")
        .getFile();

    public static final String TEST_CONCEPT = "default:TestConcept";
    public static final String TEST_CONCEPT_WITH_PARAMETER = "default:TestConceptWithParameter";
    public static final String TEST_CONSTRAINT = "default:TestConstraint";
    public static final String CUSTOM_GROUP = "customGroup";

    private static String userHome;

    private static CliConfiguration configuration;

    private PluginRepository pluginRepository;

    private StoreFactory storeFactory;

    private String neo4jVersion;

    private String jqaHome;

    /**
     * Represents the result of a CLI execution containing exit code and console
     * output.
     */
    @Builder
    @Getter
    @RequiredArgsConstructor(access = PRIVATE)
    protected static class ExecutionResult {

        private final Process process;
        private final List<String> standardConsole;
        private final List<String> errorConsole;

        public int getExitCode() {
            try {
                int exitCode = process.waitFor();
                log.info("Process finished with exit code '{}'.", exitCode);
                return exitCode;
            } catch (InterruptedException e) {
                throw new IllegalStateException("Interrupted while waiting for process to exit.", e);
            }
        }
    }

    @BeforeAll
    public static void beforeAll() {
        userHome = AbstractCLIIT.class.getResource("/userhome/")
            .getFile();
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("CLI IT", 110);
        configurationBuilder.with(Build.class, Build.NAME, "CLI IT")
            .with(Build.class, Build.TIMESTAMP, ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
        configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
            com.buschmais.jqassistant.core.store.api.configuration.Store.URI, "bolt://localhost:7687");
        configuration = ConfigurationMappingLoader.builder(CliConfiguration.class)
            .load(configurationBuilder.build());
    }

    /**
     * Reset the default store.
     */
    @BeforeEach
    public void before(DISTRIBUTION distribution) throws IOException {
        assumeThat(Runtime.version()
            .feature()).describedAs("Java runtime version")
            .isGreaterThanOrEqualTo(distribution.minRuntimeVersion.feature())
            .isLessThanOrEqualTo(distribution.maxRuntimeVersion.feature());
        this.neo4jVersion = distribution.name()
            .toLowerCase(Locale.getDefault());
        this.jqaHome = getjQAHomeDirectory(neo4jVersion);
        File workingDirectory = getWorkingDirectory();
        FileUtils.cleanDirectory(workingDirectory);
        pluginRepository = new PluginRepositoryImpl(new PluginConfigurationReaderImpl(new PluginClassLoader(AbstractCLIIT.class.getClassLoader())));
        pluginRepository.initialize();
        // The user home contains a Maven settings.xml to configure the local repository
        ArtifactProvider artifactProvider = ArtifactProviderFactory.getArtifactProvider(configuration, new File(userHome));
        storeFactory = new StoreFactory(pluginRepository.getStorePluginRepository(), artifactProvider);
    }

    private String getjQAHomeDirectory(String neo4jVersion) throws IOException {
        Properties properties = new Properties();
        properties.load(AbstractCLIIT.class.getResourceAsStream("/cli-test.properties"));
        String jqaHomeProperty = properties.getProperty("jqassistant.home");
        String projectVersionProperty = properties.getProperty("project.version");
        return new File(jqaHomeProperty + "jqassistant-commandline-" + neo4jVersion + "-" + projectVersionProperty).getAbsolutePath();
    }

    @AfterEach
    public void after() {
        if (pluginRepository != null) {
            pluginRepository.destroy();
        }
    }

    protected String getNeo4jVersion() {
        return neo4jVersion;
    }

    /**
     * Execute the shell script.
     *
     * @param args
     *     The arguments.
     * @throws IOException
     *     If an error occurs.
     * @throws InterruptedException
     *     If an error occurs.
     */
    protected ExecutionResult execute(String... args) {
        assumeTrue("Test cannot be executed on this operating system.", SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
        List<String> command = new ArrayList<>();
        if (SystemUtils.IS_OS_WINDOWS) {
            command.add("cmd.exe");
            command.add("/C");
            command.add(jqaHome + "\\bin\\jqassistant.cmd");
        } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX) {
            command.add(jqaHome + "/bin/jqassistant.sh");
        }
        command.addAll(asList(args));
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> environment = builder.environment();
        environment.put("JQASSISTANT_HOME", jqaHome);
        // add-opens is required by Neo4jv4 on JDK17
        environment.put("JQASSISTANT_OPTS", "--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED -Duser.home=" + userHome);
        // environment.put("JQASSISTANT_OPTS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000");

        File workingDirectory = getWorkingDirectory();
        builder.directory(workingDirectory);

        log.info("Executing '{}'.", command.stream()
            .collect(joining(" ")));
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new IllegalStateException("Could not execute command", e);
        }
        ConsoleReader standardConsole = new ConsoleReader(process.getInputStream(), System.out);
        ConsoleReader errorConsole = new ConsoleReader(process.getErrorStream(), System.err);
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(standardConsole);
        executorService.submit(errorConsole);
        return ExecutionResult.builder()
            .process(process)
            .standardConsole(standardConsole.getOutput())
            .errorConsole(errorConsole.getOutput())
            .build();
    }

    /**
     * Return the working directory of the test.
     *
     * @return The working directory.
     */
    protected File getWorkingDirectory() {
        File workingDirectory = new File("target" + "/" + this.getClass()
            .getSimpleName());
        workingDirectory.mkdirs();
        return workingDirectory;
    }

    /**
     * Return the default report directory of the test.
     *
     * @return The default report directory.
     */
    protected File getDefaultReportDirectory() {
        return new File(getWorkingDirectory(), Task.DEFAULT_REPORT_DIRECTORY);
    }

    protected void withStore(File directory, StoreOperation storeOperation) {
        ExecutionResult serverExecutionResult = execute("server", "-Djqassistant.store.uri=" + directory.toURI());
        try {
            Store remoteStore = storeFactory.getStore(configuration.store(), () -> directory);
            waitAtMost(30, SECONDS).untilAsserted(() -> assertThatNoException().isThrownBy(() -> remoteStore.start()));
            try {
                storeOperation.run(remoteStore);
            } finally {
                remoteStore.stop();
            }
        } finally {
            PrintWriter printWriter = new PrintWriter(serverExecutionResult.getProcess()
                .getOutputStream());
            printWriter.println();
            printWriter.flush();
        }
        if (serverExecutionResult.getExitCode() != 0) {
            throw new IllegalStateException("Cannot stop Neo4j server.");
        }
    }

    protected void withStore(StoreOperation storeOperation) {
        withStore(new File(getWorkingDirectory(), Task.DEFAULT_STORE_DIRECTORY), storeOperation);
    }

    protected interface StoreOperation {

        void run(Store store);

    }

    /**
     * Redirects process output to the given print stream.
     */
    private static class ConsoleReader implements Runnable {
        private final InputStream stream;
        private final PrintStream printStream;
        private final List<String> output;

        /**
         * Constructor.
         *
         * @param stream
         *     The stream to redirect.
         * @param printStream
         *     The target stream.
         */
        private ConsoleReader(InputStream stream, PrintStream printStream) {
            this.stream = stream;
            this.printStream = printStream;
            this.output = new ArrayList<>();
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    output.add(line);
                    printStream.println(">> " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String> getOutput() {
            return output;
        }
    }
}
