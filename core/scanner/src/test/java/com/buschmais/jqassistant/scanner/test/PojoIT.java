package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.scanner.test.set.pojo.Pojo;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.scanner.test.matcher.TypeDescriptorMatcher.classDescriptor;
import static org.junit.Assert.assertThat;

public class PojoIT extends AbstractScannerIT {

    @Test
    public void attributes() throws IOException {
        scanClasses(Pojo.class);
        String query = "MATCH (t:TYPE) WHERE t.FQN =~ '.*Pojo' RETURN t as type";
        QueryResult result = store.executeQuery(query);
        for (QueryResult.Row row : result.getRows()) {
            TypeDescriptor c = row.get("type");
            assertThat(c, classDescriptor(Pojo.class));
        }
    }
}
