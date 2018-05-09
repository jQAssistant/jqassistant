package com.buschmais.jqassistant.commandline.test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Verifies command line listing of available rules.
 */
public class AvailableRulesIT extends AbstractCLIIT {

    public AvailableRulesIT(String neo4jVersion) {
        super(neo4jVersion);
    }

    @Test
    public void listUsingRuleDirectory() throws IOException, InterruptedException {
        String rulesDirectory = AvailableRulesIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "available-rules", "-r", rulesDirectory };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode(), equalTo(0));
        List<String> console = executionResult.getErrorConsole();
        assertThat(console, hasItem(containsString(TEST_CONCEPT)));
        assertThat(console, hasItem(containsString(TEST_CONCEPT_WITH_PARAMETER)));
        assertThat(console, hasItem(containsString(CUSTOM_TEST_CONCEPT)));
        assertThat(console, hasItem(containsString("junit4:TestMethod")));
    }

    @Test
    public void listUsingRulesUrl() throws IOException, InterruptedException {
        URL rulesUrl = AvailableRulesIT.class.getResource("/rules/custom.adoc");
        String[] args = new String[] { "available-rules", "-rulesUrl", rulesUrl.toString() };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode(), equalTo(0));
        List<String> console = executionResult.getErrorConsole();
        assertThat(console, hasItem(containsString(CUSTOM_TEST_CONCEPT)));
        assertThat(console, not(hasItem(containsString("junit4:TestMethod"))));
    }
}
