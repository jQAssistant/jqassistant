package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.resolver.A;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.resolver.B;

public class TypeResolverIT extends AbstractJavaPluginIT {

    @Test
    public void referenceFirst() throws IOException {
        scanClasses(B.class);
        scanClasses(A.class);
    }

    @Test
    public void typeFirst() throws IOException {
        scanClasses(A.class);
        scanClasses(B.class);
    }

    @Test
    public void dependentArtifacts() throws IOException {
        store.beginTransaction();
        JavaArtifactDescriptor a1 = getArtifactDescriptor("a1");
        JavaArtifactDescriptor a2 = getArtifactDescriptor("a2");
        store.create(a2, DependsOnDescriptor.class, a1);
        store.commitTransaction();
        scanClasses("a1", A.class);
        scanClasses("a2", B.class);
    }

    @Test
    public void sameArtifact() throws IOException {
    }

    public void sameClassIndependentArtifacts() {

    }
}
