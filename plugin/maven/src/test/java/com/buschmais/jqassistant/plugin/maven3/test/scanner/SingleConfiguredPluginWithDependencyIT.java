package com.buschmais.jqassistant.plugin.maven3.test.scanner;


import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SingleConfiguredPluginWithDependencyIT extends AbstractJavaPluginIT {

    @BeforeEach
    void setUp() throws Exception {
        File rootDirectory = getClassesDirectory(SingleConfiguredPluginWithDependencyIT.class);
        File projectDirectory = new File(rootDirectory, "plugin/with-one-dependency");
        scanClassPathDirectory(projectDirectory);

        store.beginTransaction();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    @Test
    void configuredPluginCanBeFound() throws Exception {
        // There should be one Maven Project
        List<MavenPomXmlDescriptor> mavenPomDescriptors =
            query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName='/pom.xml' RETURN n").getColumn("n");

        assertThat(mavenPomDescriptors).hasSize(1);
        assertThat(mavenPomDescriptors.get(0).getArtifactId()).isEqualTo("with-one-dependency");

        // There should be one declared plugin
        List<MavenPluginDescriptor> pluginDescriptors =
            query("MATCH (n:Maven:Plugin) RETURN n").getColumn("n");

        assertThat(pluginDescriptors).hasSize(1);
    }

    @Test
    void dependencyOfPluginCanBeFound() {
        List<MavenArtifactDescriptor> dependencies =
            query("MATCH (p:Maven:Plugin)-[:DECLARES_DEPENDENCY]->(:Dependency)-[:TO_ARTIFACT]->(d:Maven:Artifact) RETURN d").getColumn("d");

        assertThat(dependencies).hasSize(1);

        MavenArtifactDescriptor dependency = dependencies.get(0);

        assertThat(dependency.getName()).isEqualTo("junit");
        assertThat(dependency.getGroup()).isEqualTo("junit");
        assertThat(dependency.getVersion()).isEqualTo("4.12");
        assertThat(dependency.getClassifier()).isNull();
        assertThat(dependency.getType()).isEqualTo("jar");
    }
}
