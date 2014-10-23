package com.buschmais.jqassistant.plugin.common.test.scanner;

import static com.buschmais.jqassistant.plugin.common.test.matcher.FileDescriptorMatcher.fileDescriptorMatcher;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

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
}
