package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InterfaceType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InvokeClient;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.SubClassType;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:VirtualInvokes.
 */
public class VirtualInvokesIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:VirtualInvokes" for a class implementing an
     * interface.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void invokeInterfaceMethod() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class, InvokeClient.class);
        assertThat(applyConcept("java:VirtualInvokes").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Integer> lineNumbers = query(
                "MATCH (client:Type)-[:DECLARES]->(clientMethod:Method)-[v:VIRTUAL_INVOKES]->(invokedMethod:Method)<-[:DECLARES]-(type:Type) WHERE client.name='InvokeClient' and clientMethod.name='invokeInterfaceTypeMethod' RETURN distinct v.lineNumber as lineNumber")
                        .getColumn("lineNumber");
        assertThat(lineNumbers.size(), equalTo(2));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:VirtualInvokes" for a sub class extending a class.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void invokeClassMethod() throws Exception {
        scanClasses(ClassType.class, SubClassType.class, InvokeClient.class);
        assertThat(applyConcept("java:VirtualInvokes").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Integer> lineNumbers = query(
                "MATCH (client:Type)-[:DECLARES]->(clientMethod:Method)-[v:VIRTUAL_INVOKES]->(invokedMethod:Method)<-[:DECLARES]-(type:Type) WHERE client.name='InvokeClient' and clientMethod.name='invokeClassTypeMethod' RETURN distinct v.lineNumber as lineNumber")
                        .getColumn("lineNumber");
        assertThat(lineNumbers.size(), equalTo(2));
        store.commitTransaction();
    }
}
