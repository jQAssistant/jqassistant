package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.test.matcher.ClassDescriptorMatcher;
import com.buschmais.jqassistant.scanner.test.sets.innerclass.AnonymousInnerClass;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.QueryResult;
import org.junit.Assert;
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
        scanner.scanInputStream(is, resourceName);
        store.endTransaction();
    }
}
