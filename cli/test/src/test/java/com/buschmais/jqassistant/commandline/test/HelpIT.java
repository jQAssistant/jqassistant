package com.buschmais.jqassistant.commandline.test;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Verifies command line listing of available rules.
 */
@ExtendWith(Neo4JTestTemplateInvocationContextProvider.class)
public class HelpIT extends AbstractCLIIT {

    @TestTemplate
    public void runWithoutTask() throws IOException, InterruptedException {
        verify(new String[0]);
    }

    @TestTemplate
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
