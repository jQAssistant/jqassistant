package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WithoutSourceControlManagementSectionIT extends AbstractJavaPluginIT {
    @BeforeEach
    public void setUp() throws Exception {
        File rootDirectory = getClassesDirectory(WithoutSourceControlManagementSectionIT.class);
        File projectDirectory = new File(rootDirectory, "scm/without");
        scanClassPathDirectory(projectDirectory);

        store.beginTransaction();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    // precondition
    @Test
    void pomIsFound() {
        List<MavenPluginDescriptor> pluginDescriptors =
            query("MATCH (p:Maven:Pom) RETURN p").getColumn("p");

        assertThat(pluginDescriptors).hasSize(1);
    }

    @Test
    void scmInformationisNotPresentAsItIsNotExisting() {

        List<Map<String, Object>> rows = query("MATCH (p:Maven:Pom)-[:HAS_SCM]->(s:Maven:Scm) RETURN s").getRows();

        assertThat(rows).isEmpty();
    }
}
