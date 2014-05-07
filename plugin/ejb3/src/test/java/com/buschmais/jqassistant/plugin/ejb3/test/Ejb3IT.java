package com.buschmais.jqassistant.plugin.ejb3.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.ejb3.test.set.beans.*;

/**
 * Tests for the EJB3 concepts.
 */
public class Ejb3IT extends AbstractPluginIT {

    /**
     * Verifies the concept "ejb3:StatelessSessionBean".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void statelessSessionBean() throws IOException, AnalysisException {
        scanClasses(StatelessLocalBean.class);
        applyConcept("ejb3:StatelessSessionBean");
        store.beginTransaction();
        assertThat(query("MATCH (ejb:TYPE:STATELESS) RETURN ejb").getColumn("ejb"), hasItem(typeDescriptor(StatelessLocalBean.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "ejb3:StatefulSessionBean".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void statefulSessionBean() throws IOException, AnalysisException {
        scanClasses(StatefulBean.class);
        applyConcept("ejb3:StatefulSessionBean");
        store.beginTransaction();
        assertThat(query("MATCH (ejb:TYPE:STATEFUL) RETURN ejb").getColumn("ejb"), hasItem(typeDescriptor(StatefulBean.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "ejb3:SingletonBean".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void singletonBean() throws IOException, AnalysisException {
        scanClasses(SingletonBean.class);
        applyConcept("ejb3:SingletonBean");
        store.beginTransaction();
        assertThat(query("MATCH (ejb:TYPE:SINGLETON) RETURN ejb").getColumn("ejb"), hasItem(typeDescriptor(SingletonBean.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "ejb3:MessageDrivenBean".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void messageDrivenBean() throws IOException, AnalysisException {
        scanClasses(MessageDrivenBean.class);
        applyConcept("ejb3:MessageDrivenBean");
        store.beginTransaction();
        assertThat(query("MATCH (ejb:TYPE:MESSAGEDRIVEN) RETURN ejb").getColumn("ejb"), hasItem(typeDescriptor(MessageDrivenBean.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "ejb3:Local".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void localSessionBean() throws IOException, AnalysisException {
        scanClasses(StatelessLocalBean.class);
        applyConcept("ejb3:Local");
        store.beginTransaction();
        assertThat(query("MATCH (ejb:TYPE:LOCAL) RETURN ejb").getColumn("ejb"), hasItem(typeDescriptor(StatelessLocalBean.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "ejb3:Remote".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void remoteSessionBean() throws IOException, AnalysisException {
        scanClasses(StatelessRemoteBean.class);
        applyConcept("ejb3:Remote");
        store.beginTransaction();
        assertThat(query("MATCH (ejb:TYPE:REMOTE) RETURN ejb").getColumn("ejb"), hasItem(typeDescriptor(StatelessRemoteBean.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the analysis group "ejb3:EnterpriseJavaBean".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void enterpriseJavaBean() throws IOException, AnalysisException {
        scanClasses(StatelessLocalBean.class, StatelessRemoteBean.class, StatefulBean.class, MessageDrivenBean.class);
        executeGroup("ejb3:EJB");
        store.beginTransaction();
        assertThat(
                query("MATCH (ejb:TYPE:EJB) RETURN ejb").getColumn("ejb"),
                allOf(hasItem(typeDescriptor(StatelessLocalBean.class)), hasItem(typeDescriptor(StatelessRemoteBean.class)),
                        hasItem(typeDescriptor(StatefulBean.class)), hasItem(typeDescriptor(MessageDrivenBean.class))));
        assertThat(query("MATCH (ejb:TYPE:EJB:LOCAL) RETURN ejb").getColumn("ejb"), hasItem(typeDescriptor(StatelessLocalBean.class)));
        assertThat(query("MATCH (ejb:TYPE:EJB:REMOTE) RETURN ejb").getColumn("ejb"), hasItem(typeDescriptor(StatelessRemoteBean.class)));
        store.commitTransaction();
    }
}
