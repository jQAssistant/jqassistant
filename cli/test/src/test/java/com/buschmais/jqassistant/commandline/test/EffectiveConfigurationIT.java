package com.buschmais.jqassistant.commandline.test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of effective config.
 */
class EffectiveConfigurationIT extends AbstractCLIIT {

    @DistributionTest
    void effectiveConfiguration() {
        String[] args = new String[] { "effective-configuration", "-D", "jqassistant.scan.continue-on-error=true"};
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("    continue-on-error: true"));
    }
}
