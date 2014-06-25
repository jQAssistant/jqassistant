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
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.RequestScopedBean;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.SessionScopedBean;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Tests for the CDI concepts.
 */
public class CdiIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "cdi:RequestScoped".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void requestScopedBean() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
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
    public void sessionScopedBean() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(SessionScopedBean.class);
        applyConcept("cdi:SessionScoped");
        store.beginTransaction();
        List<Object> column = query("MATCH (e:SessionScoped) RETURN e").getColumn("e");
        assertThat(column, hasItem(typeDescriptor(SessionScopedBean.class)));
        assertThat(column, hasItem(methodDescriptor(SessionScopedBean.class, "producerMethod")));
        assertThat(column, hasItem(fieldDescriptor(SessionScopedBean.class, "producerField")));
        store.commitTransaction();
    }
}
