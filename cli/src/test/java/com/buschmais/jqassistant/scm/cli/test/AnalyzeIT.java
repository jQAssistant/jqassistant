package com.buschmais.jqassistant.scm.cli.test;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.JQATask;
import com.buschmais.jqassistant.scm.cli.Main;

/**
 * Verifies command line analysis.
 */
public class AnalyzeIT extends AbstractCLIIT {

    public static final String RULES_DIRECTORY = AnalyzeIT.class.getResource("/rules").getFile();
    public static final String TEST_CONCEPT = "default:TestConcept";
    public static final String CUSTOM_TEST_CONCEPT = "default:CustomTestConcept";

    @Test
    public void defaultGroup() throws IOException {
        String rulesDirectory = AnalyzeIT.class.getResource("/rules").getFile();
        String[] args = new String[] { "analyze", "-r", rulesDirectory };
        Main.main(args);
        verifyConcepts(JQATask.DEFAULT_STORE_DIRECTORY, "default:TestConcept");
    }

    @Test
    public void customGroup() throws IOException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-groups", "customGroup" };
        Main.main(args);
        verifyConcepts(JQATask.DEFAULT_STORE_DIRECTORY, TEST_CONCEPT, CUSTOM_TEST_CONCEPT);
    }

    @Test
    public void constraint() throws IOException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-constraints", "default:TestConstraint" };
        Main.main(args);
        verifyConcepts(JQATask.DEFAULT_STORE_DIRECTORY, TEST_CONCEPT);
    }

    @Test
    public void concept() throws IOException {
        String[] args = new String[] { "analyze", "-r", RULES_DIRECTORY, "-concepts", TEST_CONCEPT + "," + CUSTOM_TEST_CONCEPT };
        Main.main(args);
        verifyConcepts(JQATask.DEFAULT_STORE_DIRECTORY, TEST_CONCEPT, CUSTOM_TEST_CONCEPT);
    }

    @Test
    public void storeDirectory() throws IOException {
        String rulesDirectory = AnalyzeIT.class.getResource("/rules").getFile();
        String customStoreDirectory = "tmp/customStore";
        String[] args = new String[] { "analyze", "-r", rulesDirectory, "-s", customStoreDirectory };
        Main.main(args);
        verifyConcepts(customStoreDirectory, "default:TestConcept");
    }

    /**
     * Verifies if the database contains the given concepts.
     *
     * @param directory
     *            The database directory.
     * @param concepts
     *            The concepts
     */
    private void verifyConcepts(String directory, String... concepts) {
        EmbeddedGraphStore store = new EmbeddedGraphStore(directory);
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
