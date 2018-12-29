package com.buschmais.jqassistant.commandline.test;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;

import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;

import static java.util.Arrays.asList;
import static org.junit.Assume.assumeTrue;

/**
 * Abstract base implementation for CLI tests.
 */
public abstract class AbstractCLIIT {

    public enum NeoVersion {
        NEO4JV3("neo4jv3");

        private final String version;

        NeoVersion(String neo4jVersion) {
            version = neo4jVersion;
        }

        @Override
        public String toString() {
            return version;
        }
    }

    public static final String RULES_DIRECTORY = AbstractCLIIT.class.getResource("/rules").getFile();

    public static final String TEST_CONCEPT = "default:TestConcept";
    public static final String TEST_CONCEPT_WITH_PARAMETER = "default:TestConceptWithParameter";
    public static final String TEST_CONSTRAINT = "default:TestConstraint";
    // defined as asciidoc
    public static final String CUSTOM_TEST_CONCEPT = "default:CustomTestConcept";

    public static final String CUSTOM_GROUP = "customGroup";

    private Properties properties = new Properties();

    private NeoVersion neo4jVersion;

    /**
     * Represents the result of a CLI execution containing exit code and console
     * output.
     */
    protected static class ExecutionResult {

        private int exitCode;
        private List<String> standardConsole;
        private List<String> errorConsole;

        /**
         * Constructor.
         *
         * @param exitCode
         *            The exit code.
         * @param standardConsole
         *            The standard console output.
         * @param errorConsole
         *            The error console output.
         */
        public ExecutionResult(int exitCode, List<String> standardConsole, List<String> errorConsole) {
            this.exitCode = exitCode;
            this.standardConsole = standardConsole;
            this.errorConsole = errorConsole;
        }

        public int getExitCode() {
            return exitCode;
        }

        public List<String> getStandardConsole() {
            return standardConsole;
        }

        public List<String> getErrorConsole() {
            return errorConsole;
        }
    }

    /**
     * Reset the default store.
     */
    @BeforeEach
    public void before(NeoVersion neoVersion) throws IOException {
        neo4jVersion = neoVersion;
        properties.load(AbstractCLIIT.class.getResourceAsStream("/cli-test.properties"));
        File workingDirectory = getWorkingDirectory();
        FileUtils.cleanDirectory(workingDirectory);
    }

    /**
     * Execute the shell script.
     *
     * @param args
     *            The arguments.
     * @throws IOException
     *             If an error occurs.
     * @throws InterruptedException
     *             If an error occurs.
     */
    protected ExecutionResult execute(String... args) throws IOException, InterruptedException {
        assumeTrue("Test cannot be executed on this operating system.",
                   SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
        String jqaHomeProperty = properties.getProperty("jqassistant.home");
        String projectVersionProperty = properties.getProperty("project.version");
        String jqaHhome = new File(jqaHomeProperty + "jqassistant-commandline-" + neo4jVersion + "-" + projectVersionProperty).getAbsolutePath();
        List<String> command = new ArrayList<>();
        if (SystemUtils.IS_OS_WINDOWS) {
            command.add("cmd.exe");
            command.add("/C");
            command.add(jqaHhome + "\\bin\\jqassistant.cmd");
        } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX) {
            command.add(jqaHhome + "/bin/jqassistant.sh");
        }
        command.addAll(asList(args));
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> environment = builder.environment();
        environment.put("JQASSISTANT_HOME", jqaHhome);
        // environment.put("JQASSISTANT_OPTS",
        // "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000");

        File workingDirectory = getWorkingDirectory();
        builder.directory(workingDirectory);

        Process process = builder.start();

        ConsoleReader standardConsole = new ConsoleReader(process.getInputStream(), System.out);
        ConsoleReader errorConsole = new ConsoleReader(process.getErrorStream(), System.err);
        Executors.newCachedThreadPool().submit(standardConsole);
        Executors.newCachedThreadPool().submit(errorConsole);
        int exitCode = process.waitFor();
        return new ExecutionResult(exitCode, standardConsole.getOutput(), errorConsole.getOutput());
    }

    /**
     * Return the working directory of the test.
     *
     * @return The working directory.
     */
    protected File getWorkingDirectory() {
        File workingDirectory = new File("target" + "/" + this.getClass().getSimpleName() + "/" + neo4jVersion);
        workingDirectory.mkdirs();
        return workingDirectory;
    }

    /**
     * Return the default store directory of the test.
     *
     * @return The default store directory.
     */
    protected File getDefaultStoreDirectory() throws IOException {
        return new File(getWorkingDirectory(), Task.DEFAULT_STORE_DIRECTORY);
    }

    /**
     * Return the default report directory of the test.
     *
     * @return The default report directory.
     */
    protected File getDefaultReportDirectory() throws IOException {
        return new File(getWorkingDirectory(), Task.DEFAULT_REPORT_DIRECTORY);
    }

    /**
     * Return the {@link Store}.
     *
     * @param directory
     *            The directory.
     * @return The {@link Store}.
     */
    protected Store getStore(File directory) {
        StoreConfiguration storeConfiguration = StoreConfiguration.builder().uri(directory.toURI()).build();
        return StoreFactory.getStore(storeConfiguration);
    }


    protected void withStore(File directory, StoreOperation storeOperation) {
        Store store = getStore(directory);
        store.start(Collections.emptyList());
        try {
            storeOperation.run(store);
        } finally {
            store.stop();
        }
    }

    interface StoreOperation {

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
         *            The stream to redirect.
         * @param printStream
         *            The target stream.
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
                    printStream.println(line);
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
