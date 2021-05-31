package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.exception.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Tests for the concepts java:Throwable, java:Error, java:Exception and
 * Java:RuntimeException.
 */
public class ThrowableIT extends AbstractJavaPluginIT {

    @BeforeEach
    public void scan() throws IOException {
        scanClasses(FirstLevelThrowable.class, SecondLevelThrowable.class, FirstLevelError.class,
                    SecondLevelError.class, FirstLevelException.class, SecondLevelException.class,
                    FirstLevelRuntimeException.class, SecondLevelRuntimeException.class);
    }

    /**
     * Verifies the concept "java:Throwable".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void throwable() throws Exception {
        assertThat(applyConcept("java:Throwable").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> elements = query("MATCH (element:Class:Throwable) RETURN element").getColumn("element");
        assertThat(elements, hasSize(2));
        assertThat(elements, hasItem(typeDescriptor(FirstLevelThrowable.class)));
        assertThat(elements, hasItem(typeDescriptor(SecondLevelThrowable.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:Error".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void error() throws Exception {
        assertThat(applyConcept("java:Error").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> elements = query("MATCH (element:Class:Error) RETURN element").getColumn("element");
        assertThat(elements, hasSize(2));
        assertThat(elements, hasItem(typeDescriptor(FirstLevelError.class)));
        assertThat(elements, hasItem(typeDescriptor(SecondLevelError.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:Exception".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void exception() throws Exception {
        assertThat(applyConcept("java:Exception").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> elements = query("MATCH (element:Class:Exception) RETURN element").getColumn("element");
        assertThat(elements, hasSize(2));
        assertThat(elements, hasItem(typeDescriptor(FirstLevelException.class)));
        assertThat(elements, hasItem(typeDescriptor(SecondLevelException.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:RuntimeException".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void runtimeException() throws Exception {
        assertThat(applyConcept("java:RuntimeException").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> elements = query("MATCH (element:Class:RuntimeException) RETURN element").getColumn("element");
        assertThat(elements, hasSize(2));
        assertThat(elements, hasItem(typeDescriptor(FirstLevelRuntimeException.class)));
        assertThat(elements, hasItem(typeDescriptor(SecondLevelRuntimeException.class)));
        store.commitTransaction();
    }
}
