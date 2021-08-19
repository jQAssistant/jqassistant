package com.buschmais.jqassistant.commandline.test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of available rules.
 */
@ExtendWith(Neo4JTestTemplateInvocationContextProvider.class)
class AvailableRulesIT extends AbstractCLIIT {

    @TestTemplate
    void listUsingRuleDirectory() throws IOException, InterruptedException {
        String rulesDirectory = AvailableRulesIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "available-rules", "-r", rulesDirectory };
        ExecutionResult executionResult = execute(args);

        assertThat(executionResult.getExitCode()).isEqualTo(0);
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT_WITH_PARAMETER));
        assertThat(console).anyMatch(item -> item.contains(CUSTOM_TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains("junit4:TestMethod"));
    }

    @TestTemplate
    void listUsingRulesUrl() throws IOException, InterruptedException {
        URL rulesUrl = AvailableRulesIT.class.getResource("/rules/custom.adoc");
        String[] args = new String[] { "available-rules", "-rulesUrl", rulesUrl.toString() };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(CUSTOM_TEST_CONCEPT));
        assertThat(console).noneMatch(item -> item.contains("junit4:TestMethod"));
    }
}
