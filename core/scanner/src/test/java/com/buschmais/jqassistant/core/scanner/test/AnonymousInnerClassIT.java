package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.test.set.innerclass.AnonymousInnerClass;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Contains test on relations between outer and inner classes.
 */
public class AnonymousInnerClassIT extends AbstractScannerIT {

    private static final String INNERCLASS_NAME = AnonymousInnerClass.class.getName() + "$1";

    /**
     * Scans an outer class.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void outerClass() throws IOException {
        scanClasses(AnonymousInnerClass.class);
        assertOuterClassContainsInnerClass();
    }

    /**
     * Scans an inner class.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void innerClass() throws IOException {
        String resourceName = "/" + INNERCLASS_NAME.replace(".", "/") + ".class";
        scanURLs(AnonymousInnerClassIT.class.getResource(resourceName));
        assertOuterClassContainsInnerClass();
    }

    /**
     * Asserts that the outer class can be fetched and contains a relation to the inner class.
     */
    private void assertOuterClassContainsInnerClass() {
        TestResult testResult = query("MATCH (outerClass:TYPE)-[:CONTAINS]->(innerClass:TYPE) RETURN outerClass");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        TypeDescriptor outerClass = (TypeDescriptor) row.get("outerClass");
        assertThat(outerClass, typeDescriptor(AnonymousInnerClass.class));
        Matcher<Iterable<? super TypeDescriptor>> matcher = hasItem(typeDescriptor(INNERCLASS_NAME));
        assertThat(outerClass.getContains(), matcher);
    }

}
