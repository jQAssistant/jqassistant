package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.StdErr;
import org.junitpioneer.jupiter.StdIo;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies functionality of the main class.
 */
@ExtendWith(MockitoExtension.class)
class HelpTaskTest extends AbstractTaskTest {

    private HelpTask helpTask;

    @BeforeEach
    final void setUp() {
        helpTask = new HelpTask();
        helpTask.initialize(pluginRepository, storeFactory);
    }

    @Test
    @StdIo
    void help(StdErr stdErr) throws CliExecutionException, ParseException {
        Options options = new Options();
        for (Option option : helpTask.getOptions()) {
            options.addOption(option);
        }
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, new String[] {});

        helpTask.configure(commandLine, mock(ConfigurationBuilder.class));
        helpTask.run(configuration, options);

        String capturedOutput = stdErr.capturedString();
        assertThat(capturedOutput).contains("---- Available Tasks: ----");
        assertThat(capturedOutput).contains("help': Lists all available tasks.");
    }
}
