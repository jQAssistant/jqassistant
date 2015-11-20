package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo.Pojo;

/**
 * Verifies functionality related to byte code and java versions.
 */
public class ByteCodeVersionIT extends AbstractJavaPluginIT {

    @Test
    public void byteCodeVersion() throws IOException, AnalysisException {
        scanClasses(Pojo.class);
        store.beginTransaction();
        List<ClassFileDescriptor> types = query("MATCH (t:Type) WHERE t.name='Pojo' RETURN t").getColumn("t");
        assertThat(types.size(), equalTo(1));
        ClassFileDescriptor pojo = types.get(0);
        assertThat(pojo.getByteCodeVersion(), equalTo(51)); // Java 7
        store.commitTransaction();

    }

    @Test
    public void javaVersion() throws Exception {
        scanClasses(Pojo.class);
        assertThat(applyConcept("java:JavaVersion").getStatus(), CoreMatchers.equalTo(SUCCESS));
        store.beginTransaction();
        List<ClassFileDescriptor> types = query("MATCH (t:Type) WHERE t.name='Pojo' and t.javaVersion='Java 7' RETURN t").getColumn("t");
        assertThat(types.size(), equalTo(1));
        ClassFileDescriptor pojo = types.get(0);
        assertThat(pojo, typeDescriptor(Pojo.class));
        store.commitTransaction();
    }
}
