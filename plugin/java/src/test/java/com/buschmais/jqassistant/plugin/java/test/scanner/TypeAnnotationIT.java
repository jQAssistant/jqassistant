package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.assertj.FieldDescriptorCondition.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

class TypeAnnotationIT extends AbstractJavaPluginIT {

    @Target(TYPE_USE)
    @Retention(RUNTIME)
    @interface Annotation {
    }

    @Annotation
    static class AnnotatedType<@Annotation T extends @Annotation Number> {

        @Annotation
        private List<@Annotation String> annotatedField;

        @Annotation
        <@Annotation T> String annotatedMethod(@Annotation String parameter) {
            return null;
        }
    }

    @Test
    void annotatedType() {
        scanClasses(AnnotatedType.class, Annotation.class);
        store.beginTransaction();
        TestResult testResult = query(
            "MATCH (t:Type:Class)-[:ANNOTATED_BY]->(:Java:ByteCode:Value:Annotation)-[:OF_TYPE]->(:Type:Annotation{fqn:$at}) RETURN t",
            Map.of("at", Annotation.class.getName()));
        List<TypeDescriptor> typeDescriptors = testResult.getColumn("t");
        assertThat(typeDescriptors).hasSize(1)
            .haveExactly(1, typeDescriptor(AnnotatedType.class));
        store.commitTransaction();
    }

    @Test
    void annotatedField() {
        scanClasses(AnnotatedType.class, Annotation.class);
        store.beginTransaction();
        TestResult testResult = query(
            "MATCH (t:Type:Class)-[:DECLARES]->(f:Field)-[:ANNOTATED_BY]->(:Java:ByteCode:Value:Annotation)-[:OF_TYPE]->(:Type:Annotation{fqn:$at}) RETURN f",
            Map.of("at", Annotation.class.getName()));
        List<FieldDescriptor> fieldDescriptors = testResult.getColumn("f");
        assertThat(fieldDescriptors).hasSize(1)
            .haveExactly(1, fieldDescriptor(AnnotatedType.class, "annotatedField"));
        store.commitTransaction();
    }

    @Test
    void annotatedMethod() throws NoSuchMethodException {
        scanClasses(AnnotatedType.class, Annotation.class);
        store.beginTransaction();
        TestResult testResult = query(
            "MATCH (t:Type:Class)-[:DECLARES]->(m:Method)-[:ANNOTATED_BY]->(:Java:ByteCode:Value:Annotation)-[:OF_TYPE]->(:Type:Annotation{fqn:$at}) RETURN m",
            Map.of("at", Annotation.class.getName()));
        List<MethodDescriptor> methodDescriptors = testResult.getColumn("m");
        assertThat(methodDescriptors).hasSize(1)
            .haveExactly(1, methodDescriptor(AnnotatedType.class, "annotatedMethod", String.class));
        store.commitTransaction();
    }

    @Test
    void annotatedMethodParameter() {
        scanClasses(AnnotatedType.class, Annotation.class);
        store.beginTransaction();
        TestResult testResult = query(
            "MATCH (t:Type:Class)-[:DECLARES]->(:Method{name:$methodName})-[:HAS]->(p:Parameter{index:0})-[:ANNOTATED_BY]->(:Java:ByteCode:Value:Annotation)-[:OF_TYPE]->(:Type:Annotation{fqn:$at}) RETURN p",
            Map.of("at", Annotation.class.getName(), "methodName", "annotatedMethod"));
        assertThat(testResult.getRows()).hasSize(1);
        store.commitTransaction();
    }
}

