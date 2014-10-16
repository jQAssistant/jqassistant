package com.buschmais.jqassistant.scm.cli.test;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;

import org.junit.Before;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.JQATask;

/**
 * Abstract base implementation for CLI tests.
 */
public abstract class AbstractCLIIT {

    private Properties properties = new Properties();

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

    protected void execute(String... args) throws IOException, InterruptedException {
        String jqaHhome = new File(properties.getProperty("jqassistant.home")).getAbsolutePath();
        List<String> command = new ArrayList<>();
        command.add("cmd.exe");
        command.add("/C");
        command.add(jqaHhome + "\\bin\\jqassistant.cmd");

        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> environment = builder.environment();
        environment.put("JQASSISTANT_HOME", jqaHhome);

        File workingDirectory = new File("target" + "/" + this.getClass().getSimpleName());
        // builder.redirectOutput(new File(workingDirectory, "console.log"));
        // builder.redirectError(new File(workingDirectory, "error.log"));
        workingDirectory.mkdirs();
        builder.directory(workingDirectory);

        final Process process = builder.start();

        Executors.newCachedThreadPool().submit(new ConsoleReader(process.getInputStream(), System.out));
        Executors.newCachedThreadPool().submit(new ConsoleReader(process.getErrorStream(), System.err));
        int i = process.waitFor();
        System.out.println("Program terminated: " + i);
    }

    private static class ConsoleReader implements Runnable {
        private final InputStream stream;
        private final PrintStream printStream;

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
