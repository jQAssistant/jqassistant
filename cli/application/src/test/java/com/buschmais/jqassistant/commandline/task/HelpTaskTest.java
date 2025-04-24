package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdErr;
import org.junitpioneer.jupiter.StdIo;

/**
 * Verifies functionality of the main class.
 */
// might be given as system properties from Maven
class HelpTaskTest {

    private final Main main = new Main();

    @Test
    @StdIo("jqassistant help")
    void skipByCommandLineOption(StdErr stdErr) throws CliExecutionException {
        main.run(new String[] { "help" });
        Assertions.assertTrue(stdErr.capturedString()
            .contains("---- Available Tasks: ----"));
        Assertions.assertTrue(stdErr.capturedString()
            .contains("help': Lists all available tasks."));
    }
}
