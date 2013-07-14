package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.test.sets.pojo.Pojo;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.QueryResult;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PojoIT extends AbstractScannerIT {

    @Test
    public void attributes() throws IOException {
        store.beginTransaction();
        scanner.scanClasses(Pojo.class);
        store.endTransaction();
        String query = "MATCH (c:CLASS) RETURN c as class";
        QueryResult result = store.executeQuery(query);
        List<String> columns = result.getColumns();
        assertEquals(1, columns.size());
        assertEquals("class", columns.get(0));
        for (Map<String, Object> row : result.getRows()) {
            assertThat(row.keySet(), everyItem(equalTo("class")));
            assertThat(row.values(), everyItem(instanceOf(ClassDescriptor.class)));
        }
    }
}
