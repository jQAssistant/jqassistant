package com.buschmais.jqassistant.plugin.common.test.scanner;

import static com.buschmais.jqassistant.plugin.common.test.matcher.FileDescriptorMatcher.fileDescriptorMatcher;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.Test;

import com.buschmais.jqassistant.core.store.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

/**
 * Verfies file/directory scanning.
 */
public class FileScannerIT extends AbstractPluginIT {

    /**
     * Scan a directory using two dependent plugins for a custom scope.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void customDirectory() throws IOException {
        store.beginTransaction();
        File classesDirectory = getClassesDirectory(FileScannerIT.class);
        FileDescriptor descriptor = getScanner().scan(classesDirectory, classesDirectory.getAbsolutePath(), CustomScope.CUSTOM);
        assertThat(descriptor, instanceOf(CustomDirectoryDescriptor.class));
        CustomDirectoryDescriptor customDirectoryDescriptor = (CustomDirectoryDescriptor) descriptor;
        assertThat(customDirectoryDescriptor.getFileName(), equalTo(classesDirectory.getAbsolutePath()));
        String expectedFilename = "/" + FileScannerIT.class.getName().replace('.', '/') + ".class";
        assertThat(customDirectoryDescriptor.getContains(), hasItem(fileDescriptorMatcher(expectedFilename)));
        assertThat(customDirectoryDescriptor.getContains(), not(hasItem(fileDescriptorMatcher("/"))));
        assertThat(customDirectoryDescriptor.getValue(), equalTo("TEST"));
        store.commitTransaction();
    }

    /**
     * Scan a directory using two dependent plugins for a custom scope.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void directoryContainsChildren() throws IOException {
        store.beginTransaction();
        File classesDirectory = getClassesDirectory(FileScannerIT.class);
        getScanner().scan(classesDirectory, classesDirectory.getAbsolutePath(), CustomScope.CUSTOM);
        String expectedFilename = "/" + FileScannerIT.class.getName().replace('.', '/') + ".class";

        Scanner scanner = new Scanner(expectedFilename).useDelimiter("/");
        StringBuffer currentName = new StringBuffer();
        FileDescriptor previous = null;
        while (scanner.hasNext()) {
            currentName.append('/').append(scanner.next());
            Map<String, Object> params = new HashMap<>();
            params.put("name", currentName.toString());
            List<FileDescriptor> files = query("match (f:File) where f.fileName={name} return f", params).getColumn("f");
            assertThat(files.size(), equalTo(1));
            FileDescriptor current = files.get(0);
            if (previous != null) {
                assertThat(previous, instanceOf(DirectoryDescriptor.class));
                assertThat(((DirectoryDescriptor) previous).getContains(), hasItem(current));
            }
            previous = current;
        }
        store.commitTransaction();
    }
}
