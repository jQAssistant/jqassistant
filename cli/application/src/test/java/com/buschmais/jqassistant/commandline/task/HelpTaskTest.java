package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Main;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdErr;
import org.junitpioneer.jupiter.StdIo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies functionality of the main class.
 */
class HelpTaskTest {

    private final Main main = new Main();

    @Test
    @StdIo
    void skipByCommandLineOption(StdErr stdErr) throws CliExecutionException {
        main.run(new String[] { "help" });

        String capturedOutput = stdErr.capturedString();
        assertThat(capturedOutput).contains("---- Available Tasks: ----");
        assertThat(capturedOutput).contains("help': Lists all available tasks.");
    }
}
