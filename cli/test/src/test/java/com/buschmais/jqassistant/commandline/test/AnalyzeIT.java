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
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line analysis.
 */
@ExtendWith(Neo4JTestTemplateInvocationContextProvider.class)
class AnalyzeIT extends AbstractCLIIT {

    @TestTemplate
    public void defaultGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    public void customGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-groups", CUSTOM_GROUP };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT, CUSTOM_TEST_CONCEPT));
    }

    @TestTemplate
    void constraint() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    void concept() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-concepts", TEST_CONCEPT + "," + CUSTOM_TEST_CONCEPT };
        assertThat(execute(args).getExitCode()).isEqualTo(0);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT, CUSTOM_TEST_CONCEPT));
    }

    @TestTemplate
    void conceptWithParameter() throws IOException, InterruptedException {
        File ruleParameters = new File(AnalyzeIT.class.getResource("/ruleparameters.properties").getPath());
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-concepts", TEST_CONCEPT_WITH_PARAMETER, "-ruleParameters",
                ruleParameters.getAbsolutePath() };
        assertThat(execute(args).getExitCode()).isEqualTo(0);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT_WITH_PARAMETER));
    }

    @TestTemplate
    void constraintFailOnSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-failOnSeverity", "major" };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    void continueOnFailure() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-failOnSeverity", "major" , "-continueOnFailure"};
        assertThat(execute(args).getExitCode()).isEqualTo(0);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    /**
     * Warn on a violated constraint but do not fail.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @TestTemplate
    void constraintWarnOnSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-warnOnSeverity", "major", "-failOnSeverity",
                "critical" };
        ExecutionResult executionResult = execute(args);
        assertThat(execute(args).getExitCode()).isEqualTo(0);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("Test constraint."));
        assertThat(console).anyMatch(item -> item.contains(TEST_CONCEPT));

    }

    @TestTemplate
    void defaultConstraintSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-failOnSeverity", "minor",
                "-defaultConstraintSeverity", "info" };
        assertThat(execute(args).getExitCode()).isEqualTo(0);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    void defaultGroupSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-failOnSeverity", "minor", "-defaultGroupSeverity", "info" };
        assertThat(execute(args).getExitCode()).isEqualTo(0);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    void storeDirectory() throws IOException, InterruptedException {
        File customStoreDir = new File(getWorkingDirectory(), "customStore");
        String rulesDirectory = AnalyzeIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "analyze", "-r", rulesDirectory, "-s", customStoreDir.getAbsolutePath() };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(customStoreDir, store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @TestTemplate
    void createReportArchive() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-createReportArchive" };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
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
            assertThat(isConceptPresent(store, concept)).withFailMessage("Expecting a result for %s", concept)
                                                        .isTrue();
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
        assertThat(result.hasResult()).isTrue();
        Long count = result.getSingleResult().get("count", Long.class);
        store.commitTransaction();
        return count == 1;
    }

}
