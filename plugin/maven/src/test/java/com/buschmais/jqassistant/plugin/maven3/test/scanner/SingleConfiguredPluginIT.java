package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class SingleConfiguredPluginIT extends AbstractJavaPluginIT {

    @BeforeEach
    public void setUp() throws Exception {
        File rootDirectory = getClassesDirectory(SingleConfiguredPluginIT.class);
        File projectDirectory = new File(rootDirectory, "plugin/single-configured-plugin");
        scanClassPathDirectory(projectDirectory);

        store.beginTransaction();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    @Test
    public void declaredPluginCanBeFoundThroughRelationShip() {
        // There should be one declared plugin
        List<MavenPluginDescriptor> pluginDescriptors =
            query("MATCH (p:Maven:Pom)-[:USES_PLUGIN]->(n:Maven:Plugin) RETURN n").getColumn("n");

        assertThat(pluginDescriptors, hasSize(1));
    }

    @Test
    public void declaredPluginCanBeFound() throws Exception {
        // There should be one Maven Project
        List<MavenPomXmlDescriptor> mavenPomDescriptors =
            query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName='/pom.xml' RETURN n").getColumn("n");

        assertThat(mavenPomDescriptors, hasSize(1));
        assertThat(mavenPomDescriptors.get(0).getArtifactId(), equalTo("single-configured-plugin"));

        // There should be one declared plugin
        List<MavenPluginDescriptor> pluginDescriptors =
            query("MATCH (n:Maven:Plugin) RETURN n").getColumn("n");

        assertThat(pluginDescriptors, hasSize(1));
    }

    @Test
    public void allPropertiesOfTheDeclaredPluginAreFound() {
        List<MavenPomXmlDescriptor> mavenPomDescriptors =
            query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName='/pom.xml' RETURN n").getColumn("n");

        assertThat(mavenPomDescriptors, hasSize(1));
        assertThat(mavenPomDescriptors.get(0).getArtifactId(), equalTo("single-configured-plugin"));

        // There should be one declared plugin
        List<Boolean> inherited = query("MATCH (n:Maven:Plugin) RETURN n.inherited AS i").getColumn("i");

        assertThat(inherited, Matchers.containsInAnyOrder(Boolean.FALSE));
    }
}
