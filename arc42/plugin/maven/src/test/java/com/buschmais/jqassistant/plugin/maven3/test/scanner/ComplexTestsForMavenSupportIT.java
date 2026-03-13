package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexTestsForMavenSupportIT extends AbstractJavaPluginIT {

    @AfterEach
    void commitTransaction() {
        // @todo Check if there is an open TX. There is some support on the current master for this.
        // Oliver B. Fischer, 2016-03-01
        store.commitTransaction();
    }

    @BeforeEach
    void scanDirectoryWithTestData() throws Exception {
        File rootDir = getClassesDirectory(ComplexTestsForMavenSupportIT.class);
        File scanRoot = new File(rootDir, "project-with-idea-config");

        scanClassPathDirectory(scanRoot);
    }

    @Test
    void shouldFind7Files() throws Exception {
        store.beginTransaction();

        List<FileDescriptor> files = query("MATCH (f:Xml:File) RETURN f").getColumn("f");

        assertThat(files).hasSize(7);
    }

    @Test
    void shouldFind2Directories() throws Exception {
        store.beginTransaction();

        List<DirectoryDescriptor> directories = query("MATCH (d:Directory) RETURN d").getColumn("d");

        assertThat(directories).hasSize(2);
    }

    @Test
    void shouldFindOneMavenPOM() throws Exception {
        store.beginTransaction();

        List<FileDescriptor> directories = query("MATCH (x:Maven:Pom:Xml) RETURN x").getColumn("x");

        assertThat(directories.stream().map(d -> d.getFileName()).collect(Collectors.toList())).containsExactly("/pom.xml");
        assertThat(directories).hasSize(1);
    }
}
