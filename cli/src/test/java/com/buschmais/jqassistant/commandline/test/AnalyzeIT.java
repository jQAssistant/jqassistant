package com.buschmais.jqassistant.commandline.test;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Verifies command line analysis.
 */
public class AnalyzeIT extends com.buschmais.jqassistant.commandline.test.AbstractCLIIT {

    @Test
    public void defaultGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    @Test
    public void customGroup() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-groups", CUSTOM_GROUP };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT, CUSTOM_TEST_CONCEPT);
    }

    @Test
    public void constraint() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    @Test
    public void concept() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-concepts", TEST_CONCEPT + "," + CUSTOM_TEST_CONCEPT };
        assertThat(execute(args).getExitCode(), equalTo(0));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT, CUSTOM_TEST_CONCEPT);
    }

    @Test
    public void constraintSeverity() throws IOException, InterruptedException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", TEST_CONSTRAINT, "-severity", "info" };
        assertThat(execute(args).getExitCode(), equalTo(2));
        verifyConcepts(getDefaultStoreDirectory(), TEST_CONCEPT);
    }

    @Test
    public void storeDirectory() throws IOException, InterruptedException {
        String rulesDirectory = AnalyzeIT.class.getResource("/rules").getFile();
        String customStoreDirectory = "tmp/customStore";
        String[] args = new String[] { "analyze", "-r", rulesDirectory, "-s", customStoreDirectory };
        assertThat(execute(args).getExitCode(), equalTo(0));
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
