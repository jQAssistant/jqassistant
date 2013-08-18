package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.JavaType;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.test.set.pojo.Pojo;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PojoIT extends AbstractScannerIT {

    @Test
    public void attributes() throws IOException {
        scanClasses(Pojo.class);
        TestResult testResult = executeQuery("MATCH (t:TYPE) WHERE t.FQN =~ '.*Pojo' RETURN t as types");
        assertThat(testResult.getRows().size(), equalTo(1));
        TypeDescriptor typeDescriptor = (TypeDescriptor) testResult.getRows().get(0).get("types");
        assertThat(typeDescriptor, is(typeDescriptor(Pojo.class)));
        assertThat(typeDescriptor.getJavaType(), is(JavaType.CLASS));
    }

}
