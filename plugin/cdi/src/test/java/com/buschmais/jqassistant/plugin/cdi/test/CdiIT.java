package com.buschmais.jqassistant.plugin.cdi.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.ApplicationScopedBean;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.ConversationScopedBean;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.DependentBean;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.RequestScopedBean;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.SessionScopedBean;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Tests for the CDI concepts.
 */
public class CdiIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "cdi:Dependent".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void dependent() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(DependentBean.class);
        applyConcept("cdi:Dependent");
        store.beginTransaction();
        List<Object> column = query("MATCH (e:Dependent) RETURN e").getColumn("e");
        assertThat(column, hasItem(typeDescriptor(DependentBean.class)));
        assertThat(column, hasItem(methodDescriptor(DependentBean.class, "producerMethod")));
        assertThat(column, hasItem(fieldDescriptor(DependentBean.class, "producerField")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "cdi:RequestScoped".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void requestScoped() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(RequestScopedBean.class);
        applyConcept("cdi:RequestScoped");
        store.beginTransaction();
        List<Object> column = query("MATCH (e:RequestScoped) RETURN e").getColumn("e");
        assertThat(column, hasItem(typeDescriptor(RequestScopedBean.class)));
        assertThat(column, hasItem(methodDescriptor(RequestScopedBean.class, "producerMethod")));
        assertThat(column, hasItem(fieldDescriptor(RequestScopedBean.class, "producerField")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "cdi:SessionScoped".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void sessionScoped() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(SessionScopedBean.class);
        applyConcept("cdi:SessionScoped");
        store.beginTransaction();
        List<Object> column = query("MATCH (e:SessionScoped) RETURN e").getColumn("e");
        assertThat(column, hasItem(typeDescriptor(SessionScopedBean.class)));
        assertThat(column, hasItem(methodDescriptor(SessionScopedBean.class, "producerMethod")));
        assertThat(column, hasItem(fieldDescriptor(SessionScopedBean.class, "producerField")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "cdi:ConversationScoped".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void conversationScoped() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(ConversationScopedBean.class);
        applyConcept("cdi:ConversationScoped");
        store.beginTransaction();
        List<Object> column = query("MATCH (e:ConversationScoped) RETURN e").getColumn("e");
        assertThat(column, hasItem(typeDescriptor(ConversationScopedBean.class)));
        assertThat(column, hasItem(methodDescriptor(ConversationScopedBean.class, "producerMethod")));
        assertThat(column, hasItem(fieldDescriptor(ConversationScopedBean.class, "producerField")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "cdi:ApplicationScoped".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void applicationScoped() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(ApplicationScopedBean.class);
        applyConcept("cdi:ApplicationScoped");
        store.beginTransaction();
        List<Object> column = query("MATCH (e:ApplicationScoped) RETURN e").getColumn("e");
        assertThat(column, hasItem(typeDescriptor(ApplicationScopedBean.class)));
        assertThat(column, hasItem(methodDescriptor(ApplicationScopedBean.class, "producerMethod")));
        assertThat(column, hasItem(fieldDescriptor(ApplicationScopedBean.class, "producerField")));
        store.commitTransaction();
    }
}
