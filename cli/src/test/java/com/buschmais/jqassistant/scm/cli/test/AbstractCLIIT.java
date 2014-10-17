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

    private Properties properties = new Properties();

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
        EmbeddedGraphStore store = new EmbeddedGraphStore(JQATask.DEFAULT_STORE_DIRECTORY);
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
    protected int execute(String... args) throws IOException, InterruptedException {
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

        Executors.newCachedThreadPool().submit(new ConsoleReader(process.getInputStream(), System.out));
        Executors.newCachedThreadPool().submit(new ConsoleReader(process.getErrorStream(), System.err));
        return process.waitFor();
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
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    printStream.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
