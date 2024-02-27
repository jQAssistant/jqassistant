package com.buschmais.jqassistant.commandline.test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies command line analysis.
 */
class AnalyzeIT extends AbstractCLIIT {

    @DistributionTest
    void defaultGroup() {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void customGroup() {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.groups=" + CUSTOM_GROUP };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void constraint() {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void concept() {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=" + TEST_CONCEPT};
        assertThat(execute(args).getExitCode()).isZero();
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void conceptWithParameter() {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.concepts=" + TEST_CONCEPT_WITH_PARAMETER, "-D", "jqassistant.analyze.rule-parameters.\"testParam\"=TestValue" };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(store -> verifyConcepts(store, TEST_CONCEPT_WITH_PARAMETER));
    }

    @DistributionTest
    void constraintFailOnSeverity() {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT, "-D", "jqassistant.analyze.report.fail-on-severity=major" };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void continueOnFailure() {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT, "-D", "jqassistant.analyze.report.continue-on-failure=true" };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    /**
     * Warn on a violated constraint but do not fail.
     */
    @DistributionTest
    void constraintWarnOnSeverity() {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT, "-D", "jqassistant.analyze.report.warn-on-severity=major", "-D",
            "jqassistant.analyze.report.fail-on-severity=critical" };
        ExecutionResult executionResult = execute(args);
        assertThat(executionResult.getExitCode()).isZero();
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
        List<String> console = executionResult.getErrorConsole();
        assertThat(console).anyMatch(item -> item.contains("Test constraint."))
            .anyMatch(item -> item.contains(TEST_CONCEPT));
    }

    @DistributionTest
    void defaultConstraintSeverity()  {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.constraints=" + TEST_CONSTRAINT, "-D", "jqassistant.analyze.report.fail-on-severity=minor", "-D",
            "jqassistant.analyze.rule.default-constraint-severity=info" };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void defaultGroupSeverity()  {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.report.fail-on-severity=minor", "-D", "jqassistant.analyze.rule.default-group-severity=info" };
        assertThat(execute(args).getExitCode()).isZero();
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void storeUri()  {
        File customStoreDir = new File(getWorkingDirectory(), "customStore");
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.store.uri=" + customStoreDir.toURI() };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(customStoreDir, store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void createReportArchive()  {
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.analyze.report.create-archive" };
        assertThat(execute(args).getExitCode()).isEqualTo(2);
        withStore(store -> verifyConcepts(store, TEST_CONCEPT));
    }

    @DistributionTest
    void apoc() {
        File configFile = new File(ScanIT.class.getResource("/.jqassistant-analyze-apoc-" + getNeo4jVersion() + ".yml")
            .getFile());
        String[] args = new String[] { "analyze", "-configurationLocations", configFile.getAbsolutePath(), "-D",
            "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D", "jqassistant.analyze.concepts=it-apoc:APOCHelp", "-D",
            "jqassistant.store.embedded.neo4j-plugin-directory=jqassistant/plugins/" };
        assertThat(execute(args).getExitCode()).isEqualTo(0);
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
