package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenScmDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class WithSourceControlManagementSectionIT extends AbstractJavaPluginIT {
    @BeforeEach
    public void setUp() throws Exception {
        File rootDirectory = getClassesDirectory(SingleConfiguredPluginIT.class);
        File projectDirectory = new File(rootDirectory, "scm/with");
        scanClassPathDirectory(projectDirectory);

        store.beginTransaction();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    // precondition
    @Test
    public void pomIsFound() {
        List<MavenPluginDescriptor> pluginDescriptors =
            query("MATCH (p:Maven:Pom) RETURN p").getColumn("p");

        assertThat(pluginDescriptors, hasSize(1));
    }

    @Test
    public void scmInformationArePartOfTheGraphModel() {
        List<MavenScmDescriptor> scmDescriptors =
            query("MATCH (p:Maven:Pom)-[:HAS_SCM]->(s:Maven:Scm) RETURN s").getColumn("s");

        assertThat(scmDescriptors, hasSize(1));
    }

    @Test
    public void scmInformationCanBeFoundViaLabel() {
        List<MavenScmDescriptor> scmDescriptors =
            query("MATCH (s:Maven:Scm) RETURN s").getColumn("s");

        assertThat(scmDescriptors, hasSize(1));
    }

    @Test
    public void propertiesOfScmInformationAreSet() {
        List<MavenScmDescriptor> scmDescriptors =
            query("MATCH (p:Maven:Pom)-[:HAS_SCM]->(s:Maven:Scm) RETURN s").getColumn("s");

        MavenScmDescriptor scmDescriptor = scmDescriptors.get(0);

        assertThat(scmDescriptor.getConnection(), equalTo("abc"));
        assertThat(scmDescriptor.getDeveloperConnection(), equalTo("def"));
        assertThat(scmDescriptor.getTag(), equalTo("none"));
        assertThat(scmDescriptor.getUrl(), equalTo("http://none"));
    }
}
