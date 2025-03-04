package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.scanner.model.DependentDirectoryDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.common.test.assertj.FileDescriptorCondition.fileDescriptor;
import static java.nio.file.Files.createSymbolicLink;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies file/directory scanning.
 */
class FileScannerIT extends AbstractPluginIT {

    /**
     * Scan a directory using two dependent plugins for a custom scope.
     */
    @Test
    void customDirectory() {
        store.beginTransaction();
        File classesDirectory = getClassesDirectory(FileScannerIT.class);
        FileDescriptor descriptor = getScanner().scan(classesDirectory, classesDirectory.getAbsolutePath(), DefaultScope.NONE);
        assertThat(descriptor).isInstanceOf(DirectoryDescriptor.class);
        DependentDirectoryDescriptor customDirectoryDescriptor = (DependentDirectoryDescriptor) descriptor;
        String expectedDirectoryName = classesDirectory.getAbsolutePath()
            .replace("\\", "/");
        assertThat(customDirectoryDescriptor.getFileName()).isEqualTo(expectedDirectoryName);
        String expectedFileName = "/" + FileScannerIT.class.getName()
            .replace('.', '/') + ".class";
        assertThat(customDirectoryDescriptor.getContains()).haveAtLeastOne(fileDescriptor(expectedFileName));
        assertThat(customDirectoryDescriptor.getContains()).doNotHave(fileDescriptor("/"));
        assertThat(customDirectoryDescriptor.getValue()).isEqualTo("TEST");
        store.commitTransaction();
    }

    /**
     * Scan a directory using two dependent plugins for a custom scope.
     */
    @Test
    void directoryContainsChildren() {
        store.beginTransaction();
        File classesDirectory = getClassesDirectory(FileScannerIT.class);
        getScanner().scan(classesDirectory, classesDirectory.getAbsolutePath(), DefaultScope.NONE);
        String expectedFilename = "/" + FileScannerIT.class.getName()
            .replace('.', '/') + ".class";

        Scanner scanner = new Scanner(expectedFilename).useDelimiter("/");
        StringBuilder currentName = new StringBuilder();
        FileDescriptor previous = null;
        while (scanner.hasNext()) {
            currentName.append('/')
                .append(scanner.next());
            Map<String, Object> params = new HashMap<>();
            params.put("name", currentName.toString());
            List<FileDescriptor> files = query("match (f:File) where f.fileName=$name return f", params).getColumn("f");
            assertThat(files.size()).isEqualTo(1);
            FileDescriptor current = files.get(0);
            if (previous != null) {
                assertThat(previous).isInstanceOf(DirectoryDescriptor.class);
                assertThat(((DirectoryDescriptor) previous).getContains()).contains(current);
            }
            previous = current;
        }
        store.commitTransaction();
    }

    @Test
    void followSymbolicLinksDisabled() throws IOException {
        store.beginTransaction();
        DirectoryDescriptor directoryDescriptor = scanSymbolicLink(emptyMap());
        assertThat(directoryDescriptor.getContains()).hasSize(1);
        store.commitTransaction();
    }

    @Test
    void followSymbolicLinksEnabled() throws IOException {
        store.beginTransaction();
        DirectoryDescriptor directoryDescriptor = scanSymbolicLink(Map.of("directory.follow-symbolic-links", "true"));
        assertThat(directoryDescriptor.getContains()).hasSizeGreaterThan(1);
        store.commitTransaction();
    }

    private DirectoryDescriptor scanSymbolicLink(Map<String, Object> properties) throws IOException {
        File classesDirectory = getClassesDirectory(FileScannerIT.class);
        File symLink = new File("target/test-classes-link");
        if (symLink.exists()) {
            symLink.delete();
        }
        createSymbolicLink(symLink.toPath(), classesDirectory.toPath());
        return getScanner(properties).scan(symLink, symLink.getAbsolutePath(), DefaultScope.NONE);
    }
}
