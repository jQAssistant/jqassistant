package com.buschmais.jqassistant.commandline.test;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of effective rules.
 */
class EffectiveRulesIT extends AbstractCLIIT {

    @Test
    void defaultGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "effective-rules", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONSTRAINT));
        assertThat(console).noneMatch(item -> item.contains("junit4:TestMethod"));
    }

    @Test
    void customGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "effective-rules", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.groups=" + CUSTOM_GROUP };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONSTRAINT));
        assertThat(console).noneMatch(item -> item.contains("junit4:TestMethod"));
    }

    @Test
    void concept() throws IOException, InterruptedException {
        String[] args = new String[] { "effective-rules", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=junit4:TestMethod" };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);

        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("junit4:TestMethod"));
    }
}
