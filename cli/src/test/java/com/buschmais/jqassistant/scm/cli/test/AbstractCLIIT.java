package com.buschmais.jqassistant.scm.cli.test;

import static org.junit.Assume.assumeTrue;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;

import org.apache.commons.lang.SystemUtils;
import org.junit.Before;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.JQATask;

/**
 * Abstract base implementation for CLI tests.
 */
public abstract class AbstractCLIIT {

    public static final String RULES_DIRECTORY = AnalyzeIT.class.getResource("/rules").getFile();

    public static final String TEST_CONCEPT = "default:TestConcept";
    public static final String TEST_CONSTRAINT = "default:TestConstraint";
    public static final String CUSTOM_TEST_CONCEPT = "default:CustomTestConcept";

    public static final String CUSTOM_GROUP = "customGroup";

    private Properties properties = new Properties();

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
     * Constructor.
     */
    protected AbstractCLIIT() {
        try {
            properties.load(AbstractCLIIT.class.getResourceAsStream("/cli-test.properties"));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read cli-test.properties.", e);
        }
    }

    /**
     * Reset the default store.
     */
    @Before
    public void before() {
        EmbeddedGraphStore store = new EmbeddedGraphStore(getDefaultStoreDirectory().getAbsolutePath());
        store.start(Collections.<Class<?>> emptyList());
        store.reset();
        store.stop();
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
        assumeTrue("Test cannot be executed on this operating system.", SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX);
        String jqaHhome = new File(properties.getProperty("jqassistant.home")).getAbsolutePath();
        List<String> command = new ArrayList<>();
        if (SystemUtils.IS_OS_WINDOWS) {
            command.add("cmd.exe");
            command.add("/C");
            command.add(jqaHhome + "\\bin\\jqassistant.cmd");
        } else if (SystemUtils.IS_OS_LINUX) {
            command.add(jqaHhome + "/bin/jqassistant.sh");
        }
        command.addAll(Arrays.asList(args));

        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> environment = builder.environment();
        environment.put("JQASSISTANT_HOME", jqaHhome);

        File workingDirectory = getWorkingDirectory();
        builder.directory(workingDirectory);

        final Process process = builder.start();

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
        File workingDirectory = new File("target" + "/" + this.getClass().getSimpleName());
        workingDirectory.mkdirs();
        return workingDirectory;
    }

    /**
     * Return the default store directory of the test.
     *
     * @return The default store directory.
     */
    protected File getDefaultStoreDirectory() {
        return new File(getWorkingDirectory(), JQATask.DEFAULT_STORE_DIRECTORY);
    }

    /**
     * Return the default report directory of the test.
     *
     * @return The default report directory.
     */
    protected File getDefaultReportDirectory() {
        return new File(getWorkingDirectory(), JQATask.DEFAULT_REPORT_DIRECTORY);
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

        public List<String> getOutput() {
            return output;
        }
    }
}
