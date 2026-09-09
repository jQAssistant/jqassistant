package com.buschmais.jqassistant.plugin.java.test.rules;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.lifecycleannotation.ManagedResource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

class LifecycleAnnotationIT extends AbstractJavaPluginIT {

    @ParameterizedTest
    @ValueSource(classes = { ManagedResource.JavaEE.class, ManagedResource.JakartaEE.class })
    void postConstruct(Class<?> type) throws NoSuchMethodException, RuleException {
        scanClasses(type);
        applyConcept("java:PostConstruct");
        store.beginTransaction();
        MethodDescriptor m = store.executeQuery("MATCH (m:PostConstruct:Method) RETURN m")
            .getSingleResult()
            .get("m", MethodDescriptor.class);
        assertThat(m).is(methodDescriptor(type, "postConstruct"));
    }

    @ParameterizedTest
    @ValueSource(classes = { ManagedResource.JavaEE.class, ManagedResource.JakartaEE.class })
    void preDestroy(Class<?> type) throws NoSuchMethodException, RuleException {
        scanClasses(type);
        applyConcept("java:PreDestroy");
        store.beginTransaction();
        MethodDescriptor m = store.executeQuery("MATCH (m:PreDestroy:Method) RETURN m")
            .getSingleResult()
            .get("m", MethodDescriptor.class);
        assertThat(m).is(methodDescriptor(type, "preDestroy"));
    }
}
