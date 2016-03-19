package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlDocumentDescriptor;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ComplexTestsForMavenSupportIT extends AbstractJavaPluginIT {

    @After
    public void commitTransaction() {
        // @todo Check if there is an open TX. There is some support on the current master for this.
        // Oliver B. Fischer, 2016-03-01
        store.commitTransaction();
    }

    @Before
    public void scanDirectoryWithTestData() throws Exception {
        File rootDir = getClassesDirectory(ComplexTestsForMavenSupportIT.class);
        File scanRoot = new File(rootDir, "project-with-idea-config");

        scanClassPathDirectory(scanRoot);
    }

    @Ignore
    @Test
    public void shouldFind8Files() throws Exception {
        store.beginTransaction();

        List<FileDescriptor> files = query("MATCH (f:File) RETURN f").getColumn("f");
//        List<FileDescriptor> files = query("MATCH (f:File) WHERE NOT(f:Directory) RETURN f").getColumn("f");

        files.forEach(fileDescriptor -> { System.out.println(">"+fileDescriptor.getFileName()+"<"); });

        assertThat(files, hasSize(8));
    }

    @Test
    public void shouldFind2Directories() throws Exception {
        store.beginTransaction();

        List<DirectoryDescriptor> directories = query("MATCH (d:Directory) RETURN d").getColumn("d");

        assertThat(directories, hasSize(2));
    }

    @Test
    public void shouldFindOneMavenPOM() throws Exception {
        store.beginTransaction();

        List<FileDescriptor> directories = query("MATCH (x:Maven:Pom:Xml) RETURN x").getColumn("x");

        assertThat(directories.stream().map(d -> d.getFileName()).collect(Collectors.toList()),
                   Matchers.contains("/pom.xml"));
        assertThat(directories, hasSize(1));
    }

    @Test
    public void shouldFind1XMLFile() throws Exception {
        store.beginTransaction();

        /*
         * Currently we are not able to find arbitrary XML files.
         * The XML file scanner accepts files only if it is in the
         * scope XmlScope.DOCUMENT.
         * See XMLFileScannerPlugin.
         * Oliver B. Fischer, 2016-03-01
         */
        List<FileDescriptor> directories = query("MATCH (x:File:Xml) RETURN x").getColumn("x");

        List<String> filePaths = directories.stream().map(d -> d.getFileName()).collect(Collectors.toList());
        assertThat(filePaths, Matchers.hasItem("/pom.xml"));
        assertThat(directories, hasSize(1));
    }
}
