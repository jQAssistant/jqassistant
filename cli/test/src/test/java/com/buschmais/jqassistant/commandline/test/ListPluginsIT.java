package com.buschmais.jqassistant.commandline.test;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies, that commandline supports the list-plugins command
 */
@ExtendWith(Neo4JTestTemplateInvocationContextProvider.class)
class ListPluginsIT extends AbstractCLIIT {

    @TestTemplate
    void supportsListingOfPlugins() throws IOException, InterruptedException {
        verify(new String[] { "list-plugins" });
    }

    private void verify(String[] args) throws IOException, InterruptedException {
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);
        List<String> console = executionResult.getStandardConsole();
        assertThat(console).anyMatch(item -> item.contains("jQAssistant GraphML Plugin (jqa.plugin.graphml)"));
        assertThat(console).anyMatch(item -> item.contains("jQAssistant RDBMS Plugin (jqa.plugin.rdbms)"));
    }
}
