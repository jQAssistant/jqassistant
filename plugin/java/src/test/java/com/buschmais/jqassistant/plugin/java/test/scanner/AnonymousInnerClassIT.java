package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.innerclass.AnonymousInnerClass;

/**
 * Contains test on relations between outer and inner classes.
 */
public class AnonymousInnerClassIT extends AbstractJavaPluginIT {

    private static final String INNERCLASS_NAME = AnonymousInnerClass.class.getName() + "$1";

    /**
     * Scans an outer class.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    @Ignore("Scanning only the outer class without their inner classes is currently not supported.")
    public void outerClass() throws IOException {
        scanClasses(AnonymousInnerClass.class);
        assertOuterClassContainsInnerClass();
    }

    /**
     * Scans an inner class.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void innerClass() throws IOException, ClassNotFoundException {
        scanInnerClass(AnonymousInnerClass.class, "1");
        assertOuterClassContainsInnerClass();
    }

    /**
     * Scans first the outer then the inner class.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void outerAndInnerClass() throws IOException, ClassNotFoundException {
        scanClasses(AnonymousInnerClass.class);
        scanInnerClass(AnonymousInnerClass.class, "1");
        assertOuterClassContainsInnerClass();
    }

    /**
     * Scans first the inner then the outer class.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void innerAndOuterClass() throws IOException, ClassNotFoundException {
        scanInnerClass(AnonymousInnerClass.class, "1");
        scanClasses(AnonymousInnerClass.class);
        assertOuterClassContainsInnerClass();
    }

    /**
     * Asserts that the outer class can be fetched and contains a relation to
     * the inner class.
     */
    private void assertOuterClassContainsInnerClass() {
        store.beginTransaction();
        TestResult testResult = query("MATCH (outerClass:Type)-[:DECLARES]->(innerClass:Type) RETURN outerClass, innerClass");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        TypeDescriptor outerClass = (TypeDescriptor) row.get("outerClass");
        assertThat(outerClass, typeDescriptor(AnonymousInnerClass.class));
        TypeDescriptor innerClass = (TypeDescriptor) row.get("innerClass");
        assertThat(innerClass, typeDescriptor(INNERCLASS_NAME));
        store.commitTransaction();
    }

}
