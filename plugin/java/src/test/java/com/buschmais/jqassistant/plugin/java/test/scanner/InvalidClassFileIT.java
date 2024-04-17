package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractVirtualFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Verifies graceful handling of invalid class files.
 */
class InvalidClassFileIT extends AbstractJavaPluginIT {

    @Test
    void classFileWithHeaderOnly() throws IOException {
        final String path = "/com.buschmais.Test.class";
        List<? extends FileDescriptor> fileDescriptors;
        try (FileResource fileResource = new AbstractVirtualFileResource() {
            @Override
            public InputStream createStream() {
                return new ByteArrayInputStream(ClassFileScannerPlugin.CAFEBABE);
            }

            @Override
            protected String getRelativePath() {
                return path;
            }
        }) {
            fileDescriptors = execute(ARTIFACT_ID, (artifact, scanner) -> {
                FileDescriptor fileDescriptor = scanner.scan(fileResource, path, JavaScope.CLASSPATH);
                return singletonList(fileDescriptor);
            });
        }
        store.beginTransaction();
        assertThat(fileDescriptors.size(), equalTo(1));
        FileDescriptor fileDescriptor = fileDescriptors.get(0);
        assertThat(fileDescriptor, instanceOf(ClassFileDescriptor.class));
        ClassFileDescriptor classFileDescriptor = (ClassFileDescriptor) fileDescriptor;
        assertThat(classFileDescriptor.getFileName(), equalTo(path));
        assertThat(classFileDescriptor.isValid(), equalTo(false));
        store.commitTransaction();

    }

    @Test
    void validClass() throws IOException {
        scanClasses(InvalidClassFileIT.class);
        store.beginTransaction();
        List<FileDescriptor> fileDescriptors = query("MATCH (c:Class:File) RETURN c").getColumn("c");
        assertThat(fileDescriptors.size(), equalTo(1));
        FileDescriptor fileDescriptor = fileDescriptors.get(0);
        assertThat(fileDescriptor, instanceOf(ClassFileDescriptor.class));
        ClassFileDescriptor classFileDescriptor = (ClassFileDescriptor) fileDescriptor;
        assertThat(classFileDescriptor.isValid(), equalTo(true));
        store.commitTransaction();

    }
}
