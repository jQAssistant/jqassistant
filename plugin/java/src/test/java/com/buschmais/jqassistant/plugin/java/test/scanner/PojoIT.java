package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.JavaType;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo.Pojo;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class PojoIT extends AbstractPluginIT {

    @Test
    public void attributes() throws IOException {
        scanClasses(Pojo.class);
        TestResult testResult = query("MATCH t:TYPE:CLASS WHERE t.FQN =~ '.*Pojo' RETURN t as types");
        assertThat(testResult.getRows().size(), equalTo(1));
        TypeDescriptor typeDescriptor = (TypeDescriptor) testResult.getRows().get(0).get("types");
        assertThat(typeDescriptor, is(typeDescriptor(Pojo.class)));
        assertThat(typeDescriptor.getJavaType(), is(JavaType.CLASS));
        assertThat(query("MATCH t:TYPE:CLASS WHERE t.FQN =~ '.*Pojo' RETURN t.SIGNATURE as signature").getColumn("signature"), hasItem(equalTo("Pojo")));

        testResult = query("MATCH t:TYPE:CLASS-[:CONTAINS]->f:FIELD RETURN f.SIGNATURE as signature, f.NAME as name");
        assertThat(testResult.getColumn("signature"), allOf(hasItem(equalTo("java.lang.String stringValue")), hasItem(equalTo("int intValue"))));
        assertThat(testResult.getColumn("name"), allOf(hasItem(equalTo("stringValue")), hasItem(equalTo("intValue"))));

        testResult = query("MATCH t:TYPE:CLASS-[:CONTAINS]->m:METHOD RETURN m.SIGNATURE as signature, m.NAME as name");
        assertThat(testResult.getColumn("signature"), allOf(hasItem(equalTo("java.lang.String getStringValue()")), hasItem(equalTo("void setStringValue(java.lang.String)")), hasItem(equalTo("int getIntValue()")), hasItem(equalTo("void setIntValue(int)"))));
        assertThat(testResult.getColumn("name"), allOf(hasItem(equalTo("getStringValue")), hasItem(equalTo("setStringValue")), hasItem(equalTo("getIntValue")), hasItem(equalTo("setIntValue"))));
    }

}
