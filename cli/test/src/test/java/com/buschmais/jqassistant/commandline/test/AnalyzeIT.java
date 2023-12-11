package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.jupiter.api.Test;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line analysis.
 */
class AnalyzeIT extends AbstractCLIIT {

    @Test
    void defaultGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @Test
    void customGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.groups=" + CUSTOM_GROUP };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @Test
    void constraint() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @Test
    void concept() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=" + TEST_CONCEPT};
        assertThat(execute(args).getExitCode()).isZero();
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @Test
    void conceptWithParameter() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=" + TEST_CONCEPT_WITH_PARAMETER, "-D", "jqassistant.analyze.rule-parameters.\"testParam\"=TestValue" };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT_WITH_PARAMETER));
    }

    @Test
    void constraintFailOnSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT, "-D", "jqassistant.analyze.report.fail-on-severity=major" };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @Test
    void continueOnFailure() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT, "-D", "jqassistant.analyze.report.continue-on-failure=true" };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    /**
     * Warn on a violated constraint but do not fail.
     */
    @Test
    void constraintWarnOnSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT, "-D", "jqassistant.analyze.report.warn-on-severity=major", "-D",
            "jqassistant.analyze.report.fail-on-severity=critical" };
        ExecutionResult executionResult = execute(args);
        assertThat(execute(args).getExitCode()).isZero();
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("Test constraint."))
            .anyMatch(item -> item.contains(TEST_CONCEPT));
    }

    @Test
    void defaultConstraintSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT, "-D", "jqassistant.analyze.report.fail-on-severity=minor", "-D",
            "jqassistant.analyze.rule.default-constraint-severity=info" };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @Test
    void defaultGroupSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.report.fail-on-severity=minor", "-D", "jqassistant.analyze.rule.default-group-severity=info" };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @Test
    void storeUri() throws IOException, InterruptedException {
        File customStoreDir = new File(getWorkingDirectory(), "customStore");
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.store.uri=" + customStoreDir.toURI() };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(customStoreDir, store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @Test
    void createReportArchive() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY,
            "-Djqassistant.analyze.report.create-archive" };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(getDefaultStoreDirectory(), store -> verifyConcepts(store, TEST_CONCEPT));
    }

    /**
     * Verifies if the database contains the given concepts.
     *
     * @param store
     *     The {@link Store}.
     * @param concepts
     *     The concepts
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
     *     The store
     * @param concept
     *     The concept
     * @return <code>true</code> if the concept is represented in the database.
     */
    private boolean isConceptPresent(Store store, String concept) {
        store.beginTransaction();
        Map<String, Object> params = new HashMap<>();
        params.put("concept", concept);
        Result<CompositeRowObject> result = store.executeQuery("match (c:Concept) where c.id=$concept return count(c) as count", params);
        assertThat(result.hasResult()).isTrue();
        Long count = result.getSingleResult()
            .get("count", Long.class);
        store.commitTransaction();
        return count == 1;
    }

}
