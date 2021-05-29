package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.LambdaMethod;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class LambdaMethodIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:LambdaMethod".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void lambdaMethod() throws Exception {
        scanClasses(LambdaMethod.class);

        assertThat(applyConcept("java:LambdaMethod").getStatus(), equalTo(SUCCESS));

        store.beginTransaction();
        TestResult result = query("MATCH (m:Method)-[:DECLARES_LAMBDA]->(l:Method:Lambda) RETURN m,l");
        assertThat(result.getRows().size(), equalTo(1));
        assertThat(result.getColumn("m"), hasItem(methodDescriptor(LambdaMethod.class, "withLambda")));
        List<MethodDescriptor> lambdaMethods = result.getColumn("l");
        assertThat(lambdaMethods.size(), equalTo(1));
        MethodDescriptor methodDescriptor = lambdaMethods.get(0);
        assertThat(methodDescriptor.getName(), equalTo("lambda$withLambda$0"));
        store.commitTransaction();
    }

}
