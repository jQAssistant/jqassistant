package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.VariableDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo.Pojo;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

class PojoIT extends AbstractJavaPluginIT {

    @Override
    protected Map<String, Object> getScannerProperties() {
        return Map.of(ClassFileScannerPlugin.PROPERTY_INCLUDE_LOCAL_VARIABLES, true);
    }

    @Test
    void attributes() {
        scanClasses(Pojo.class);
        store.beginTransaction();
        TestResult testResult = query("MATCH (t:Java:ByteCode:Type:Class) WHERE t.fqn =~ '.*Pojo' RETURN t as types");
        assertThat(testResult.getRows()).hasSize(1);
        ClassFileDescriptor typeDescriptor = (ClassFileDescriptor) testResult.getRows()
            .get(0)
            .get("types");
        assertThat(typeDescriptor).is(typeDescriptor(Pojo.class));
        assertThat(typeDescriptor.getFileName()).isEqualTo("/" + Pojo.class.getName()
            .replace('.', '/') + ".class");
        assertThat(typeDescriptor.getSourceFileName()).isEqualTo(Pojo.class.getSimpleName() + ".java");
        assertThat(query("MATCH (t:Java:ByteCode:Type:Class) WHERE t.fqn =~ '.*Pojo' RETURN t.name as name").getColumn("name"), hasItem(equalTo("Pojo")));

        testResult = query("MATCH (t:Java:ByteCode:Type:Class)-[:DECLARES]->(f:Java:ByteCode:Field) RETURN f.signature as signature, f.name as name");
        assertThat(testResult.getColumn("signature")).contains("java.lang.String stringValue", "int intValue");
        assertThat(testResult.getColumn("name")).contains("stringValue", "intValue");

        testResult = query("MATCH (t:Type:Class)-[:DECLARES]->(m:Java:ByteCode:Method) RETURN m.signature as signature, m.name as name");
        assertThat(testResult.getColumn("signature")).contains("java.lang.String getStringValue()", "void setStringValue(java.lang.String)",
            "int getIntValue()", "void setIntValue(int)");
        assertThat(testResult.getColumn("name")).contains("getStringValue", "setStringValue", "getIntValue", "setIntValue");
        store.commitTransaction();
    }

    @Test
    void lineNumbers() {
        scanClasses(Pojo.class);
        store.beginTransaction();
        List<int[]> lines = query("MATCH (:Method{name:'hashCode'})-[i:INVOKES]->() return i.lineNumber as lines").getColumn("lines");
        assertThat(lines).hasSize(1);
        lines = query("MATCH (:Java:ByteCode:Method{name:'getStringValue'})-[i:READS]->() return i.lineNumber as lines").getColumn("lines");
        assertThat(lines).hasSize(1);
        lines = query("MATCH (:Java:ByteCode:Method{name:'setStringValue'})-[i:WRITES]->() return i.lineNumber as lines").getColumn("lines");
        assertThat(lines).hasSize(1);
        List<MethodDescriptor> hashCodeList = query("MATCH (hashCode:Method{name:'hashCode'}) return hashCode ").getColumn("hashCode");
        assertThat(hashCodeList).hasSize(1);
        MethodDescriptor hashCode = hashCodeList.get(0);
        assertThat(hashCode.getFirstLineNumber()).isNotNull();
        assertThat(hashCode.getLastLineNumber()).isNotNull();
        assertThat(hashCode.getLastLineNumber()).isGreaterThan(hashCode.getFirstLineNumber());
        List<MethodDescriptor> equalsList = query("MATCH (equals:Method{name:'equals'}) return equals ").getColumn("equals");
        assertThat(equalsList).hasSize(1);
        MethodDescriptor equals = equalsList.get(0);
        assertThat(equals.getEffectiveLineCount()).isEqualTo(8);
        store.commitTransaction();
    }

    @Test
    void variables() {
        scanClasses(Pojo.class);
        store.beginTransaction();
        List<VariableDescriptor> variables = query("MATCH (:Java:ByteCode:Method{name:'equals'})-[:DECLARES]->(v:Java:ByteCode:Variable) return v").getColumn(
            "v");
        assertThat(variables).hasSize(2);
        Map<String, VariableDescriptor> map = new HashMap<>();
        for (VariableDescriptor variable : variables) {
            map.put(variable.getName(), variable);
        }
        VariableDescriptor o = map.get("o");
        assertThat(o).isNotNull();
        assertThat(o.getSignature()).isEqualTo(Object.class.getName() + " o");
        assertThat(o.getType()).is(typeDescriptor(Object.class));
        VariableDescriptor pojo = map.get("pojo");
        assertThat(pojo).isNotNull();
        assertThat(pojo.getSignature()).isEqualTo(Pojo.class.getName() + " pojo");
        assertThat(pojo.getType()).is(typeDescriptor(Pojo.class));
        store.commitTransaction();
    }
}
