package com.buschmais.jqassistant.commandline.test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of available rules.
 */
class HelpIT extends AbstractCLIIT {

    @DistributionTest
    void runWithoutTask() {
        verify(new String[0]);
    }

    @DistributionTest
    void helpOption() {
        verify(new String[] { "help" });
    }

    private void verify(String[] args) {
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getStandardConsole();
        assertThat(console).anyMatch(item -> item.contains("usage: com.buschmais.jqassistant.commandline.Main <task> [options]"));
    }

    @DistributionTest
    void plugins() {
        ExecutionResult executionResult = execute("help");
        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getErrorConsole();

        assertThat(console).anyMatch(item -> matches(item, "jQAssistant Common Plugin", "jqa.plugin.common"));
        assertThat(console).anyMatch(item -> matches(item, "jQAssistant Core Analysis Plugin", "jqa.core.analysis.plugin"));
        assertThat(console).anyMatch(item -> matches(item, "jQAssistant Core Report Plugin", "jqa.core.report.plugin"));
        assertThat(console).anyMatch(item -> matches(item, "jQAssistant Java Plugin", "jqa.plugin.java"));
        assertThat(console).anyMatch(item -> matches(item, "jQAssistant Java Testing Plugin", "jqa.plugin.java.testing"));
        assertThat(console).anyMatch(item -> matches(item, "jQAssistant JSON Plugin", "jqa.plugin.json"));
        assertThat(console).anyMatch(item -> matches(item, "jQAssistant Maven 3 Plugin", "jqa.plugin.maven3"));
        assertThat(console).anyMatch(item -> matches(item, "jQAssistant XML Plugin", "jqa.plugin.xml"));
        assertThat(console).anyMatch(item -> matches(item, "jQAssistant YAML 2 Plugin", "jqa.plugin.yaml2"));
    }

    private static boolean matches(String item, String expectedName, String expectedId) {
        return item.contains(expectedName) && item.contains("[" + expectedId + "]");
    }

}
