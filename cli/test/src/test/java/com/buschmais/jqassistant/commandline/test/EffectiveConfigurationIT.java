package com.buschmais.jqassistant.commandline.test;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line listing of effective config.
 */
class EffectiveConfigurationIT extends AbstractCLIIT {

    @Test
    void effectiveConfiguration() throws IOException, InterruptedException {
        String[] args = new String[] { "effective-configuration", "-D", "jqassistant.scan.continue-on-error=true"};
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isEqualTo(0);
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("    continue-on-error: true"));
    }
}
