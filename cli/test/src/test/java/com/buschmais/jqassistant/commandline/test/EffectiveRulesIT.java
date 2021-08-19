package com.buschmais.jqassistant.commandline.test;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of effective rules.
 */
@ExtendWith(Neo4JTestTemplateInvocationContextProvider.class)
class EffectiveRulesIT extends AbstractCLIIT {

    @TestTemplate
    void defaultGroup() throws IOException, InterruptedException {
        String rulesDirectory = EffectiveRulesIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "effective-rules", "-r", rulesDirectory };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONSTRAINT));
        assertThat(console).noneMatch(item -> item.contains(CUSTOM_TEST_CONCEPT));
        assertThat(console).noneMatch(item -> item.contains("junit4:TestMethod"));
    }

    @TestTemplate
    void customGroup() throws IOException, InterruptedException {
        String rulesDirectory = EffectiveRulesIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "effective-rules", "-r", rulesDirectory, "-groups", CUSTOM_GROUP };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONSTRAINT));
        assertThat(console).anyMatch(item -> item.contains(CUSTOM_TEST_CONCEPT));
        assertThat(console).noneMatch(item -> item.contains("junit4:TestMethod"));
    }

    @TestTemplate
    void concept() throws IOException, InterruptedException {
        String rulesDirectory = EffectiveRulesIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "effective-rules", "-r", rulesDirectory, "-concepts", "junit4:TestMethod" };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);

        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("junit4:TestMethod"));
    }
}
