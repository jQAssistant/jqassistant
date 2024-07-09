package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo.Pojo;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Verifies functionality related to byte code and java versions.
 */
class ByteCodeVersionIT extends AbstractJavaPluginIT {

    @Test
    void byteCodeVersion() {
        scanClasses(Pojo.class);
        store.beginTransaction();
        List<ClassFileDescriptor> types = query("MATCH (t:Type) WHERE t.name='Pojo' RETURN t").getColumn("t");
        assertThat(types).hasSize(1);
        ClassFileDescriptor pojo = types.get(0);
        assertThat(pojo.getByteCodeVersion()).isGreaterThan(52); // Java 8
        store.commitTransaction();

    }

    @Test
    void javaVersion() throws Exception {
        scanClasses(Pojo.class);
        assertThat(applyConcept("java:JavaVersion").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        List<ClassFileDescriptor> types = query("MATCH (t:Type) WHERE t.name='Pojo' and t.javaVersion starts with 'Java ' RETURN t").getColumn("t");
        assertThat(types).hasSize(1);
        ClassFileDescriptor pojo = types.get(0);
        assertThat(pojo, typeDescriptor(Pojo.class));
        store.commitTransaction();
    }
}
