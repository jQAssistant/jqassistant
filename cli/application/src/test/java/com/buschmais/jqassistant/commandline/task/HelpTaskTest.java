package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

/**
 * Verifies functionality of the main class.
 */
// might be given as system properties from Maven
@ClearSystemProperty(key = "jqassistant.store.uri")
class HelpTaskTest {

    private final Main main = new Main();

    @Test
    @StdIo("jqassistant help")
    void skipByCommandLineOption(StdOut stdOut) throws CliExecutionException {
        main.run(new String[] { "help" });
        Assertions.assertTrue(stdOut.capturedString()
            .contains("---- Available Tasks: ----"));
        Assertions.assertTrue(stdOut.capturedString()
            .contains("help': Lists all available options."));

    }
}
