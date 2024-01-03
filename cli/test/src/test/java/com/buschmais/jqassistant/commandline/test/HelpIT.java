package com.buschmais.jqassistant.commandline.test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of available rules.
 */
class HelpIT extends AbstractCLIIT {

    @DistributionTest
    void runWithoutTask()  {
        verify(new String[0]);
    }

    @DistributionTest
    void helpOption()  {
        verify(new String[] { "help" });
    }

    private void verify(String[] args)  {
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getStandardConsole();
        assertThat(console).anyMatch(item -> item.contains("usage: com.buschmais.jqassistant.commandline.Main <task> [options]"));
    }

}
