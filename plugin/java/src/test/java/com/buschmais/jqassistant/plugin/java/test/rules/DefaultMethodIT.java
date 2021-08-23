package com.buschmais.jqassistant.plugin.java.test.rules;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.DefaultMethod;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Tests for the concept java:DefaultMethod.
 */
class DefaultMethodIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:DefaultMethod".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    void defaultMethod() throws Exception {
        scanClasses(DefaultMethod.class);
        assertThat(applyConcept("java:DefaultMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query("MATCH (m:Method:Default) RETURN m");
        assertThat(result.getRows().size(), equalTo(1));
        assertThat(result.getColumn("m"), hasItem(methodDescriptor(DefaultMethod.class, "add", int.class, int.class)));
        store.commitTransaction();
    }
}
