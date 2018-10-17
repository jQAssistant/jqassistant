package com.buschmais.jqassistant.commandline.test;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Verifies command line listing of available rules.
 */
public class HelpIT extends AbstractCLIIT {

    public HelpIT(String neo4jVersion) {
        super(neo4jVersion);
    }

    @Test
    public void runWithoutTask() throws IOException, InterruptedException {
        verify(new String[0]);
    }

    @Test
    public void helpOption() throws IOException, InterruptedException {
        verify(new String[] { "-help}" });
    }

    private void verify(String[] args) throws IOException, InterruptedException {
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode(), equalTo(1));
        List<String> console = executionResult.getStandardConsole();
        assertThat(console, hasItem(containsString("usage: com.buschmais.jqassistant.commandline.Main <task> [options]")));
    }

}
