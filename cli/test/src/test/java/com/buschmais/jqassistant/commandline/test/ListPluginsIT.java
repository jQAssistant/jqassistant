package com.buschmais.jqassistant.commandline.test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies, that commandline supports the list-plugins command
 */
class ListPluginsIT extends AbstractCLIIT {

    @DistributionTest
    void supportsListingOfPlugins()  {
        verify(new String[] { "list-plugins" });
    }

    private void verify(String[] args)  {
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getStandardConsole();
        assertThat(console).hasSize(9);
        assertThat(console).anyMatch(item -> item.contains("jQAssistant Common Plugin (jqa.plugin.common)"));
        assertThat(console).anyMatch(item -> item.contains("jQAssistant Core Analysis Plugin (jqa.core.analysis.plugin)"));
        assertThat(console).anyMatch(item -> item.contains("jQAssistant Core Report Plugin (jqa.core.report.plugin)"));
        assertThat(console).anyMatch(item -> item.contains("jQAssistant Java Plugin (jqa.plugin.java)"));
        assertThat(console).anyMatch(item -> item.contains("jQAssistant JSON Plugin (jqa.plugin.json)"));
        assertThat(console).anyMatch(item -> item.contains("jQAssistant Maven 3 Plugin (jqa.plugin.maven3)"));
        assertThat(console).anyMatch(item -> item.contains("jQAssistant XML Plugin (jqa.plugin.xml)"));
        assertThat(console).anyMatch(item -> item.contains("jQAssistant YAML 2 Plugin (jqa.plugin.yaml2)"));
    }
}
