package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.LambdaMethod;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

class LambdaMethodIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:LambdaMethod".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    void lambdaMethod() throws Exception {
        scanClasses(LambdaMethod.class);

        store.beginTransaction();
        TestResult result = query("MATCH (m:Method{name:'withLambda'})-[:INVOKES]->(l:Method:Lambda) RETURN m,l");
        assertThat(result.getRows().size(), equalTo(1));
        assertThat(result.getColumn("m"), hasItem(methodDescriptor(LambdaMethod.class, "withLambda")));
        List<MethodDescriptor> lambdaMethods = result.getColumn("l");
        assertThat(lambdaMethods.size(), equalTo(1));
        MethodDescriptor methodDescriptor = lambdaMethods.get(0);
        assertThat(methodDescriptor.getName(), equalTo("lambda$withLambda$0"));
        store.commitTransaction();
    }

    @Test
    void methodReference() throws Exception {
        scanClasses(LambdaMethod.class);

        store.beginTransaction();
        TestResult result = query("MATCH (m:Method{name:'withMethodReference'})-[:INVOKES]->(:Method{signature:'void println(java.lang.String)'})<-[:DECLARES]-(:Type{fqn:'java.io.PrintStream'}) RETURN m");
        assertThat(result.getRows().size(), equalTo(1));
        assertThat(result.getColumn("m"), hasItem(methodDescriptor(LambdaMethod.class, "withMethodReference")));
        store.commitTransaction();
    }
}
