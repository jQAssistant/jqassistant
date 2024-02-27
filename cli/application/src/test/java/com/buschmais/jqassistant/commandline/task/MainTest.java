package com.buschmais.jqassistant.commandline.task;

import java.util.List;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Main;
import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;

import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verifies functionality of the main class.
 */
@ClearSystemProperty(key = "jqassistant.skip") // might be given as system property to Maven
class MainTest {

    private Main main;

    @BeforeEach
    void setUp() {
        this.main = new com.buschmais.jqassistant.commandline.Main() {
            @Override
            protected void executeTasks(List<Task> tasks, CliConfiguration configuration, PluginRepository pluginRepository, ArtifactProvider artifactProvider,
                Options options) {
                fail("(No task must be executed");
            }
        };
    }

    @Test
    void skipByCommandLineOption() throws CliExecutionException {
        main.run(new String[] { "help", "-D", "jqassistant.skip=true" });
    }

    @Test
    @SetSystemProperty(key = "jqassistant.skip", value = "true")
    void skipBySystemProperty() throws CliExecutionException {
        main.run(new String[] { "help" });
    }

    @Test
    @SetEnvironmentVariable(key = "JQASSISTANT_SKIP", value = "true")
    void skipByEnvironmentVariable() throws CliExecutionException {
        main.run(new String[] { "help" });
    }
}
