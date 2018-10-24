package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenScmDescriptor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

public class WithoutSourceControlManagementSectionIT extends AbstractJavaPluginIT {
    @Before
    public void setUp() throws Exception {
        File rootDirectory = getClassesDirectory(SingleConfiguredPluginIT.class);
        File projectDirectory = new File(rootDirectory, "scm/without");
        scanClassPathDirectory(projectDirectory);

        store.beginTransaction();
    }

    @After
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
    public void scmInformationisNotPresentAsItIsNotExisting() {
        List<MavenScmDescriptor> scmDescriptors =
            query("MATCH (p:Maven:Pom)-[:HAS_SCM]->(s:Maven:Scm) RETURN s").getColumn("s");

        assertThat(scmDescriptors, anyOf(hasSize(0), nullValue()));
    }
}
