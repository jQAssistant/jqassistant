package com.buschmais.jqassistant.commandline.test;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * Verifies command line analysis.
 */
public class AnalyzeIT extends com.buschmais.jqassistant.commandline.test.AbstractCLIIT {

    @Test
    public void defaultGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY };
        assertThat(execute(args).getExitCode(), equalTo(2));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    @Test
    public void customGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-groups", CUSTOM_GROUP };
        assertThat(execute(args).getExitCode(), equalTo(2));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT, CUSTOM_TEST_CONCEPT);
    }

    @Test
    public void constraint() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT };
        assertThat(execute(args).getExitCode(), equalTo(2));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    @Test
    public void concept() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-concepts", TEST_CONCEPT + "," + CUSTOM_TEST_CONCEPT };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT, CUSTOM_TEST_CONCEPT);
    }

    @Test
    public void conceptWithParameter() throws IOException, InterruptedException {
        File ruleParameters = new File(AnalyzeIT.class.getResource("/ruleparameters.properties").getPath());
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-concepts", TEST_CONCEPT_WITH_PARAMETER, "-ruleParameters",
                ruleParameters.getAbsolutePath() };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT_WITH_PARAMETER);
    }

    @Test
    public void constraintSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-severity", "critical" };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    @Test
    public void constraintFailOnSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-failOnSeverity", "major" };
        assertThat(execute(args).getExitCode(), equalTo(2));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    /**
     * Warn on a violated constraint but do not fail.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void constraintWarnOnSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-warnOnSeverity", "major", "-failOnSeverity",
                "critical" };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
        List<String> console = executionResult.getErrorConsole();
        assertThat(console, hasItem(containsString("Test constraint."))); // The description
        assertThat(console, hasItem(containsString(TEST_CONSTRAINT)));

    }

    @Test
    public void defaultConstraintSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-failOnSeverity",
                "minor", "-defaultConstraintSeverity", "info" };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    @Test
    public void defaultGroupSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-failOnSeverity", "minor", "-defaultGroupSeverity", "info" };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    @Test
    public void storeDirectory() throws IOException, InterruptedException {
        String rulesDirectory = AnalyzeIT.class.getResource("/rules").getFile();
        String customStoreDirectory = "tmp/customStore";
        String[] args = new String[] { "analyze", "-r", rulesDirectory, "-s", customStoreDirectory };
        assertThat(execute(args).getExitCode(), equalTo(2));
        verifyConcepts(new File(getWorkingDirectory(), customStoreDirectory), TEST_CONCEPT);
    }

    /**
     * Verifies if the database contains the given concepts.
     *
     * @param directory
     *            The database directory.
     * @param concepts
     *            The concepts
     */
    private void verifyConcepts(File directory, String... concepts) {
        EmbeddedGraphStore store = new EmbeddedGraphStore(directory.getAbsolutePath());
        store.start(Collections.<Class<?>> emptyList());
        for (String concept : concepts) {
            assertThat("Expecting a result for " + concept, isConceptPresent(store, concept), equalTo(true));
        }
        store.stop();
    }

    /**
     * Determine if a specific concept is in the database.
     *
     * @param store
     *            The store
     * @param concept
     *            The concept
     * @return <code>true</code> if the concept is represented in the database.
     */
    private boolean isConceptPresent(EmbeddedGraphStore store, String concept) {
        store.beginTransaction();
        Map<String, Object> params = new HashMap<>();
        params.put("concept", concept);
        Result<CompositeRowObject> result = store.executeQuery("match (c:Concept) where c.id={concept} return count(c) as count", params);
        assertThat(result.hasResult(), equalTo(true));
        Long count = result.getSingleResult().get("count", Long.class);
        store.commitTransaction();
        return count.longValue() == 1;
    }

}
