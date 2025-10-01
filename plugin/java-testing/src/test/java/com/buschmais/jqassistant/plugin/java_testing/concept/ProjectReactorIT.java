package com.buschmais.jqassistant.plugin.java_testing.concept;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ProjectReactorIT extends AbstractJavaPluginIT {

    @Test
    void projectReactorAssertMethod() throws Exception {
        scanClasses(AssertExample.class);

        final Result<Concept> conceptResult = applyConcept("projectreactor:AssertMethod");
        assertThat(conceptResult.getStatus()).isEqualTo(SUCCESS);

        store.beginTransaction();

        assertThat(conceptResult.getRows().size()).isEqualTo(1);
        assertThat(conceptResult.getRows()
            .get(0)
            .getColumns()
            .get("assertMethod")
            .getValue()).asInstanceOf(type(MethodDescriptor.class))
            .is(methodDescriptor(StepVerifier.class, "verify"));

        verifyResultGraph();

        store.commitTransaction();
    }

    // Expects an open transaction
    private void verifyResultGraph() throws NoSuchMethodException {
        final TestResult methodQueryResult = query(
            "MATCH (testMethod:Method)-[:INVOKES]->(assertMethod:Method) "
                + "WHERE assertMethod:ProjectReactor:Assert "
                + "RETURN testMethod, assertMethod");
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("testMethod"))
            .haveExactly(1, methodDescriptor(AssertExample.class, "projectReactorAssertExampleMethod"));
        assertThat(methodQueryResult.<MethodDescriptor>getColumn("assertMethod"))
            .haveExactly(1, methodDescriptor(StepVerifier.class, "verify"));
    }

}
