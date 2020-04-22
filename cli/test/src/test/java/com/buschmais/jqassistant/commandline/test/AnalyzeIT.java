package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Verifies command line analysis.
 */
@ExtendWith(Neo4JTestTemplateInvocationContextProvider.class)
public class AnalyzeIT extends AbstractCLIIT {

    @TestTemplate
    public void defaultGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY };
        assertThat(execute(args).getExitCode(), equalTo(2));
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    public void customGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-groups", CUSTOM_GROUP };
        assertThat(execute(args).getExitCode(), equalTo(2));
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT, CUSTOM_TEST_CONCEPT));
    }

    @TestTemplate
    public void constraint() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT };
        assertThat(execute(args).getExitCode(), equalTo(2));
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    public void concept() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-concepts", TEST_CONCEPT + "," + CUSTOM_TEST_CONCEPT };
        assertThat(execute(args).getExitCode(), equalTo(0));
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT, CUSTOM_TEST_CONCEPT));
    }

    @TestTemplate
    public void conceptWithParameter() throws IOException, InterruptedException {
        File ruleParameters = new File(AnalyzeIT.class.getResource("/ruleparameters.properties").getPath());
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-concepts", TEST_CONCEPT_WITH_PARAMETER, "-ruleParameters",
                ruleParameters.getAbsolutePath() };
        assertThat(execute(args).getExitCode(), equalTo(0));
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT_WITH_PARAMETER));
    }

    @TestTemplate
    public void constraintFailOnSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-failOnSeverity", "major" };
        assertThat(execute(args).getExitCode(), equalTo(2));
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    /**
     * Warn on a violated constraint but do not fail.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @TestTemplate
    public void constraintWarnOnSeverity() throws IOException, InterruptedException {
        String[] args = new String[]{"analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT,
                                     "-warnOnSeverity", "major", "-failOnSeverity", "critical"};
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode(), equalTo(0));
        withStore(getDefaultStoreDirectory(), store ->  verifyConcepts(store, TEST_CONCEPT));
        List<String> console = executionResult.getErrorConsole();
        assertThat(console, hasItem(containsString("Test constraint."))); // The description
        assertThat(console, hasItem(containsString(TEST_CONSTRAINT)));

    }

    @TestTemplate
    public void defaultConstraintSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-failOnSeverity", "minor",
                "-defaultConstraintSeverity", "info" };
        assertThat(execute(args).getExitCode(), equalTo(0));
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    public void defaultGroupSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-failOnSeverity", "minor", "-defaultGroupSeverity", "info" };
        assertThat(execute(args).getExitCode(), equalTo(0));
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    public void storeDirectory() throws IOException, InterruptedException {
        File customStoreDir = new File(getWorkingDirectory(), "customStore");
        String rulesDirectory = AnalyzeIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "analyze", "-r", rulesDirectory, "-s", customStoreDir.getAbsolutePath() };
        assertThat(execute(args).getExitCode(), equalTo(2));
        withStore(customStoreDir, store -> verifyConcepts(store, TEST_CONCEPT));
    }

    /**
     * Verifies if the database contains the given concepts.
     *
     * @param store
     *            The {@link Store}.
     * @param concepts
     *            The concepts
     */
    private void verifyConcepts(Store store, String... concepts) {
        for (String concept : concepts) {
            assertThat("Expecting a result for " + concept, isConceptPresent(store, concept), equalTo(true));
        }
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
    private boolean isConceptPresent(Store store, String concept) {
        store.beginTransaction();
        Map<String, Object> params = new HashMap<>();
        params.put("concept", concept);
        Result<CompositeRowObject> result = store.executeQuery("match (c:Concept) where c.id=$concept return count(c) as count", params);
        assertThat(result.hasResult(), equalTo(true));
        Long count = result.getSingleResult().get("count", Long.class);
        store.commitTransaction();
        return count.longValue() == 1;
    }

}
