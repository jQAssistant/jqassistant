package com.buschmais.jqassistant.commandline.test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of available rules.
 */
class AvailableRulesIT extends AbstractCLIIT {

    @DistributionTest
    void listUsingRuleDirectory() {
        String[] args = new String[] { "available-rules", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY };
        ExecutionResult executionResult = execute(args);

        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT_WITH_PARAMETER));
        assertThat(console).anyMatch(item -> item.contains("junit4:TestMethod"));
    }
}
