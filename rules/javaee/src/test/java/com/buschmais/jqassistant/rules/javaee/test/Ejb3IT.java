package com.buschmais.jqassistant.rules.javaee.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.rules.javaee.test.set.ejb3.*;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the EJB3 concepts.
 */
public class Ejb3IT extends AbstractAnalysisIT {

    /**
     * Verifies the concept "ejb3:StatelessSessionBean".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void statelessSessionBean() throws IOException, AnalyzerException {
        scanClasses(StatelessLocalBean.class);
        applyConcept("ejb3:StatelessSessionBean");
        Map<String, List<Object>> columns = executeQuery("MATCH (ejb:TYPE:STATELESS) RETURN ejb").getColumns();
        assertThat(columns.get("ejb"), hasItem(typeDescriptor(StatelessLocalBean.class)));
    }

    /**
     * Verifies the concept "ejb3:StatefulSessionBean".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void statefulSessionBean() throws IOException, AnalyzerException {
        scanClasses(StatefulBean.class);
        applyConcept("ejb3:StatefulSessionBean");
        Map<String, List<Object>> columns = executeQuery("MATCH (ejb:TYPE:STATEFUL) RETURN ejb").getColumns();
        assertThat(columns.get("ejb"), hasItem(typeDescriptor(StatefulBean.class)));
    }


    /**
     * Verifies the concept "ejb3:SingletonBean".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void singletonBean() throws IOException, AnalyzerException {
        scanClasses(SingletonBean.class);
        applyConcept("ejb3:SingletonBean");
        Map<String, List<Object>> columns = executeQuery("MATCH (ejb:TYPE:SINGLETON) RETURN ejb").getColumns();
        assertThat(columns.get("ejb"), hasItem(typeDescriptor(SingletonBean.class)));
    }

    /**
     * Verifies the concept "ejb3:MessageDrivenBean".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void messageDrivenBean() throws IOException, AnalyzerException {
        scanClasses(MessageDrivenBean.class);
        applyConcept("ejb3:MessageDrivenBean");
        Map<String, List<Object>> columns = executeQuery("MATCH (ejb:TYPE:MESSAGEDRIVEN) RETURN ejb").getColumns();
        assertThat(columns.get("ejb"), hasItem(typeDescriptor(MessageDrivenBean.class)));
    }

    /**
     * Verifies the concept "ejb3:Local".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void localSessionBean() throws IOException, AnalyzerException {
        scanClasses(StatelessLocalBean.class);
        applyConcept("ejb3:Local");
        Map<String, List<Object>> columns = executeQuery("MATCH (ejb:TYPE:LOCAL) RETURN ejb").getColumns();
        assertThat(columns.get("ejb"), hasItem(typeDescriptor(StatelessLocalBean.class)));
    }

    /**
     * Verifies the concept "ejb3:Remote".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void remoteSessionBean() throws IOException, AnalyzerException {
        scanClasses(StatelessRemoteBean.class);
        applyConcept("ejb3:Remote");
        Map<String, List<Object>> columns = executeQuery("MATCH (ejb:TYPE:REMOTE) RETURN ejb").getColumns();
        assertThat(columns.get("ejb"), hasItem(typeDescriptor(StatelessRemoteBean.class)));
    }

    /**
     * Verifies the analysis group "ejb3:EnterpriseJavaBean".
     *
     * @throws IOException           If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void enterpriseJavaBean() throws IOException, AnalyzerException {
        scanClasses(StatelessLocalBean.class, StatelessRemoteBean.class, StatefulBean.class, MessageDrivenBean.class);
        executeGroup("ejb3:EJB");
        assertThat(executeQuery("MATCH (ejb:TYPE:EJB) RETURN ejb").getColumns().get("ejb"), allOf(
                hasItem(typeDescriptor(StatelessLocalBean.class)),
                hasItem(typeDescriptor(StatelessRemoteBean.class)),
                hasItem(typeDescriptor(StatefulBean.class)),
                hasItem(typeDescriptor(MessageDrivenBean.class))
        ));
        assertThat(executeQuery("MATCH (ejb:TYPE:EJB:LOCAL) RETURN ejb").getColumns().get("ejb"),
                hasItem(typeDescriptor(StatelessLocalBean.class)));
        assertThat(executeQuery("MATCH (ejb:TYPE:EJB:REMOTE) RETURN ejb").getColumns().get("ejb"),
                hasItem(typeDescriptor(StatelessRemoteBean.class)));
    }
}
