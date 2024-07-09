package com.buschmais.jqassistant.commandline.test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of effective rules.
 */
class EffectiveRulesIT extends AbstractCLIIT {

    @DistributionTest
    void defaultGroup()  {
        String[] args = new String[] { "effective-rules", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONSTRAINT));
        assertThat(console).noneMatch(item -> item.contains("junit4:TestMethod"));
    }

    @DistributionTest
    void customGroup()  {
        String[] args = new String[] { "effective-rules", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.groups=" + CUSTOM_GROUP };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONSTRAINT));
        assertThat(console).noneMatch(item -> item.contains("junit4:TestMethod"));
    }

    @DistributionTest
    void concept()  {
        String[] args = new String[] { "effective-rules", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=junit4:TestMethod" };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();

        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("junit4:TestMethod"));
    }
}
