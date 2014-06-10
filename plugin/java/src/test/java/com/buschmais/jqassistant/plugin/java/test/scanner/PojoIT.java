package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo.Pojo;

public class PojoIT extends AbstractJavaPluginIT {

    @Test
    public void attributes() throws IOException {
        scanClasses(Pojo.class);
        store.beginTransaction();
        TestResult testResult = query("MATCH (t:Type:Class) WHERE t.fqn =~ '.*Pojo' RETURN t as types");
        assertThat(testResult.getRows().size(), equalTo(1));
        TypeDescriptor typeDescriptor = (TypeDescriptor) testResult.getRows().get(0).get("types");
        assertThat(typeDescriptor, is(typeDescriptor(Pojo.class)));
        assertThat(typeDescriptor.getFileName(), equalTo("/" + Pojo.class.getName().replace('.', '/') + ".class"));
        assertThat(query("MATCH (t:Type:Class) WHERE t.fqn =~ '.*Pojo' RETURN t.name as name").getColumn("name"), hasItem(equalTo("Pojo")));

        testResult = query("MATCH (t:Type:Class)-[:DECLARES]->(f:Field) RETURN f.signature as signature, f.name as name");
        assertThat(testResult.getColumn("signature"), allOf(hasItem(equalTo("java.lang.String stringValue")), hasItem(equalTo("int intValue"))));
        assertThat(testResult.getColumn("name"), allOf(hasItem(equalTo("stringValue")), hasItem(equalTo("intValue"))));

        testResult = query("MATCH (t:Type:Class)-[:DECLARES]->(m:Method) RETURN m.signature as signature, m.name as name");
        assertThat(
                testResult.getColumn("signature"),
                allOf(hasItem(equalTo("java.lang.String getStringValue()")), hasItem(equalTo("void setStringValue(java.lang.String)")),
                        hasItem(equalTo("int getIntValue()")), hasItem(equalTo("void setIntValue(int)"))));
        assertThat(testResult.getColumn("name"),
                allOf(hasItem(equalTo("getStringValue")), hasItem(equalTo("setStringValue")), hasItem(equalTo("getIntValue")), hasItem(equalTo("setIntValue"))));
        List<int[]> lines = query("MATCH ()-[i:INVOKES]->() return i.lineNumber as lines").getColumn("lines");
        assertThat(lines.size(), equalTo(1));
        lines = query("MATCH ()-[i:READS]->() return i.lineNumber as lines").getColumn("lines");
        assertThat(lines.size(), equalTo(2));
        lines = query("MATCH ()-[i:WRITES]->() return i.lineNumber as lines").getColumn("lines");
        assertThat(lines.size(), equalTo(2));
        store.commitTransaction();
    }

}
