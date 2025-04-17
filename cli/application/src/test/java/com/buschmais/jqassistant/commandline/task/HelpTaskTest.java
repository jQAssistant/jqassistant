package com.buschmais.jqassistant.commandline.task;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Verifies functionality of the main class.
 */
// might be given as system properties from Maven
class HelpTaskTest {

    private final Main main = new Main();

    @Test
    void testHelpOutput() throws CliExecutionException {
        PrintStream original = System.err;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setErr(new PrintStream(outputStream));
            main.run(new String[] { "help" });
            String logOutput = outputStream.toString();
            Assertions.assertTrue(logOutput.contains("---- Available Tasks: ----"));
            Assertions.assertTrue(logOutput.contains("help': Lists all available tasks."));

        } finally {
            System.setErr(original);
        }
    }
}
