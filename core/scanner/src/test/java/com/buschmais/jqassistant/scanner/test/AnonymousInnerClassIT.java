package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.scanner.test.set.innerclass.AnonymousInnerClass;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.buschmais.jqassistant.scanner.test.matcher.ClassDescriptorMatcher.classDescriptor;
import static org.junit.Assert.assertThat;

public class AnonymousInnerClassIT extends AbstractScannerIT {

    @Test
    public void outerClass() throws IOException {
        scanClasses(AnonymousInnerClass.class);
        QueryResult result = store.executeQuery("MATCH (outerClass:CLASS) RETURN outerClass");
        Iterable<QueryResult.Row> rows = result.getRows();
        QueryResult.Row row = rows.iterator().next();
        ClassDescriptor outerClass = row.get("outerClass");
        assertThat(outerClass, classDescriptor(AnonymousInnerClass.class));
    }

    @Test
    public void innerClass() throws IOException {
        String resourceName = "/" + AnonymousInnerClass.class.getName().replace(".", "/") + "$1.class";
        InputStream is = AnonymousInnerClassIT.class.getResourceAsStream(resourceName);
        store.beginTransaction();
        getScanner().scanInputStream(is, resourceName);
        store.endTransaction();
    }
}
