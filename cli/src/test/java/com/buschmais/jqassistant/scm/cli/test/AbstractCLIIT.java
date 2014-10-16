package com.buschmais.jqassistant.scm.cli.test;

import java.io.*;
import java.util.*;

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
        command.add("start");
        command.add(jqaHhome + "\\bin\\jqassistant.cmd");

        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> environment = builder.environment();
        environment.put("JQASSISTANT_HOME", jqaHhome);

        File workingDirectory = new File("target" + "/" + this.getClass().getSimpleName());
        workingDirectory.mkdirs();
        builder.directory(workingDirectory);

        final Process process = builder.start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        int i = process.waitFor();
        System.out.println("Program terminated: " + i);
    }

}
