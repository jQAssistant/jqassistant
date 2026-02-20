package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import org.apache.maven.model.Activation;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.PluginExecution;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Maven4ModelHelper}.
 *
 * These tests verify graceful fallback behavior when running under Maven 3
 * (where Maven 4 compat API methods are not available).
 */
class Maven4ModelHelperTest {

    @Test
    void maven4NotAvailableOnMaven3() {
        // When running tests with Maven 3 dependencies, Maven 4 should not be detected
        assertThat(Maven4ModelHelper.isMaven4Available()).isFalse();
    }

    @Test
    void isRootReturnsNullOnMaven3() {
        Model model = new Model();
        assertThat(Maven4ModelHelper.isRoot(model)).isNull();
    }

    @Test
    void getSubprojectsReturnsEmptyOnMaven3() {
        Model model = new Model();
        assertThat(Maven4ModelHelper.getSubprojects(model)).isEmpty();
    }

    @Test
    void getConditionReturnsNullOnMaven3() {
        Activation activation = new Activation();
        assertThat(Maven4ModelHelper.getCondition(activation)).isNull();
    }

    @Test
    void getPriorityReturnsNullOnMaven3() {
        PluginExecution execution = new PluginExecution();
        assertThat(Maven4ModelHelper.getPriority(execution)).isNull();
    }

    @Test
    void getSourcesReturnsEmptyOnMaven3() {
        Build build = new Build();
        assertThat(Maven4ModelHelper.getSources(build)).isEmpty();
    }
}
