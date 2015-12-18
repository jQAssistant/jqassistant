package com.buschmais.jqassistant.plugin.cdi.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.decorator.DecoratorBean;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Tests for the decorator concepts.
 */
public class DecoratorIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "cdi:Decorator".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void decorator() throws Exception {
        scanClasses(DecoratorBean.class);
        assertThat(applyConcept("decorator:Decorator").getStatus(), equalTo(Result.Status.SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (e:Decorator) RETURN e").getColumn("e"), hasItem(typeDescriptor(DecoratorBean.class)));
        assertThat(query("MATCH (e:Field:Decorator:Delegate) RETURN e").getColumn("e"), hasItem(fieldDescriptor(DecoratorBean.class, "delegate")));
        store.commitTransaction();
    }
}
