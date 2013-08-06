package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.scanner.test.matcher.ClassDescriptorMatcher;
import com.buschmais.jqassistant.scanner.test.set.innerclass.AnonymousInnerClass;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.buschmais.jqassistant.scanner.test.matcher.ClassDescriptorMatcher.classDescriptor;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class AnonymousInnerClassIT extends AbstractScannerIT {

    private static final String INNERCLASS_NAME = AnonymousInnerClass.class.getName() + "$1";

    @Test
    public void outerClass() throws IOException {
        scanClasses(AnonymousInnerClass.class);
        TestResult testResult = executeQuery("MATCH (outerClass:CLASS)-[:CONTAINS]->(innerClass:CLASS) RETURN outerClass");
        Map<String,Object> row = testResult.getRows().get(0);
        ClassDescriptor outerClass = (ClassDescriptor) row.get("outerClass");
        assertThat(outerClass, classDescriptor(AnonymousInnerClass.class));
        Matcher<Iterable<? super ClassDescriptor>> matcher = hasItem(classDescriptor(INNERCLASS_NAME));
        assertThat(outerClass.getContains(), matcher);
    }

    @Test
    public void innerClass() throws IOException {
        String resourceName = "/" + INNERCLASS_NAME.replace(".", "/") + ".class";
        InputStream is = AnonymousInnerClassIT.class.getResourceAsStream(resourceName);
        store.beginTransaction();
        getScanner().scanInputStream(is, resourceName);
        store.endTransaction();
    }
}
