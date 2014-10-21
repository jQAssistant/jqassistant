package com.buschmais.jqassistant.scm.cli.test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

/**
 * Verifies command line listing of available rules.
 */
public class AvailableRulesIT extends AbstractCLIIT {

    public static final String TEST_CONCEPT = "default:TestConcept";
    public static final String CUSTOM_TEST_CONCEPT = "default:CustomTestConcept";

    @Test
    public void list() throws IOException, InterruptedException {
        String rulesDirectory = AvailableRulesIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "available-rules", "-r", rulesDirectory };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode(), equalTo(0));
        List<String> standardConsole = executionResult.getStandardConsole();
        assertThat(standardConsole, hasItem(containsString(TEST_CONCEPT)));
        assertThat(standardConsole, hasItem(containsString(CUSTOM_TEST_CONCEPT)));
        assertThat(standardConsole, hasItem(containsString("junit4:TestMethod")));
    }

}
