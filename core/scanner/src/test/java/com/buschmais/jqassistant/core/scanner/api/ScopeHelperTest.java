package com.buschmais.jqassistant.core.scanner.api;

import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.ScopeHelper.ScopedResource;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ScopeHelperTest {

    private ScopeHelper scopeHelper = new ScopeHelper(log);

    /**
     * Verifies parsing scoped resources from a command line, including trimming of
     * leading/trailing slashes
     */
    @Test
    void getScopedResourcesFromString() {
        List<ScopedResource> scopedResources = scopeHelper.getScopedResources(" build/images , java:classpath::build/classes ");
        verifyScopedResources(scopedResources);
    }

    /**
     * Verifies parsing scoped resources from a list.
     */
    @Test
    void getScopedResourcesFromList() {
        List<ScopedResource> scopedResources = scopeHelper.getScopedResources(asList(" build/images ", " java:classpath::build/classes "));
        verifyScopedResources(scopedResources);
    }

    private void verifyScopedResources(List<ScopedResource> scopedResources) {
        assertThat(scopedResources).hasSize(2);
        assertThat(scopedResources).containsExactly(ScopedResource.builder().resource("build/images").build(),
                ScopedResource.builder().resource("build/classes").scopeName("java:classpath").build());
    }
}
