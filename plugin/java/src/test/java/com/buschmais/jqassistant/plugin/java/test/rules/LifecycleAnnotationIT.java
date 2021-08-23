package com.buschmais.jqassistant.plugin.java.test.rules;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.lifecycleannotation.ManagedResource;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.MatcherAssert.assertThat;

class LifecycleAnnotationIT extends AbstractJavaPluginIT {

    @Test
    void postConstruct() throws NoSuchMethodException, RuleException {
        scanClasses(ManagedResource.class);
        applyConcept("java:PostConstruct");
        store.beginTransaction();
        MethodDescriptor m = store.executeQuery("MATCH (m:PostConstruct:Method) RETURN m").getSingleResult().get("m", MethodDescriptor.class);
        assertThat(m, methodDescriptor(ManagedResource.class, "postConstruct"));
    }

    @Test
    void preDestroy() throws NoSuchMethodException, RuleException {
        scanClasses(ManagedResource.class);
        applyConcept("java:PreDestroy");
        store.beginTransaction();
        MethodDescriptor m = store.executeQuery("MATCH (m:PreDestroy:Method) RETURN m").getSingleResult().get("m", MethodDescriptor.class);
        assertThat(m, methodDescriptor(ManagedResource.class, "preDestroy"));
    }
}
