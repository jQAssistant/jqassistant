package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.scanner.test.sets.pojo.Pojo;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.scanner.test.matcher.ClassDescriptorMatcher.classDescriptor;
import static org.junit.Assert.assertThat;

public class PojoIT extends AbstractScannerIT {

    @Test
    public void attributes() throws IOException {
        scanClasses(Pojo.class);
        String query = "MATCH (c:CLASS) WHERE c.FQN =~ '.*Pojo' RETURN c as class";
        QueryResult result = store.executeQuery(query);
        for (QueryResult.Row row : result.getRows()) {
            ClassDescriptor c = row.get("class");
            assertThat(c, classDescriptor(Pojo.class));
        }
    }
}
