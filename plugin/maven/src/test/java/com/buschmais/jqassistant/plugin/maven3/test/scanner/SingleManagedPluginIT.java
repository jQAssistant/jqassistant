package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class SingleManagedPluginIT extends AbstractJavaPluginIT {

    @BeforeEach
    void setUp() throws Exception {
        File rootDirectory = getClassesDirectory(SingleManagedPluginIT.class);
        File projectDirectory = new File(rootDirectory, "plugin/single-managed-plugin");
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
    void managedPluginCanBeFoundThroughRelationShip() {
        // There should be one Maven Project
        List<MavenPomXmlDescriptor> mavenPomDescriptors =
            query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName='/pom.xml' RETURN n").getColumn("n");

        assertThat(mavenPomDescriptors, hasSize(1));
        assertThat(mavenPomDescriptors.get(0).getArtifactId(), equalTo("with-one-managed-plugin"));

        // There should be one managed plugin
        List<MavenPluginDescriptor> pluginDescriptors =
            query("MATCH (p:Maven:Pom)-[:MANAGES_PLUGIN]->(n:Maven:Plugin) RETURN n").getColumn("n");

        assertThat(pluginDescriptors, hasSize(1));
    }
}
