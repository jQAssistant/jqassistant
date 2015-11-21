package com.buschmais.jqassistant.plugin.java.test.scanner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Verifies graceful handling of invalid class files.
 */
public class InvalidClassFileIT extends AbstractJavaPluginIT {

    @Test
    public void classFileWithHeaderOnly() throws IOException, AnalysisException {
        final String path = "/com.buschmais.Test.class";
        final FileResource fileResource = new AbstractFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                return new ByteArrayInputStream(ClassFileScannerPlugin.CAFEBABE);
            }
        };
        List<? extends FileDescriptor> fileDescriptors = execute(ARTIFACT_ID, new ScanClassPathOperation() {
            @Override
            public List<FileDescriptor> scan(JavaArtifactFileDescriptor artifact, Scanner scanner) {
                FileDescriptor fileDescriptor = scanner.scan(fileResource, path, JavaScope.CLASSPATH);
                return Collections.singletonList(fileDescriptor);
            }
        });
        store.beginTransaction();
        assertThat(fileDescriptors.size(), equalTo(1));
        FileDescriptor fileDescriptor = fileDescriptors.get(0);
        assertThat(fileDescriptor, instanceOf(ClassFileDescriptor.class));
        ClassFileDescriptor classFileDescriptor = (ClassFileDescriptor) fileDescriptor;
        assertThat(classFileDescriptor.getFileName(), equalTo(path));
        assertThat(classFileDescriptor.isInvalid(), equalTo(true));
        store.commitTransaction();

    }

}
