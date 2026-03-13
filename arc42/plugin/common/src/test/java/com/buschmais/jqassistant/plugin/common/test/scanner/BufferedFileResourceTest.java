package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.BufferedFileResource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BufferedFileResourceTest {

    private static final String stream = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Test
    void reUseBuffer() throws IOException {
        readAndVerify(stream.length());
    }

    @Test
    void exhaustBuffer() throws IOException {
        readAndVerify(5);
    }

    private void readAndVerify(int bufferSize) throws IOException {
        FileResource fileResource = getFileResource(stream);
        BufferedFileResource bufferedFileResource = new BufferedFileResource(fileResource, bufferSize);
        byte[] target = new byte[10];
        String expectedStream = stream.substring(0, 10);
        readAndVerify(bufferedFileResource, target, expectedStream);
        readAndVerify(bufferedFileResource, target, expectedStream);
    }

    private void readAndVerify(BufferedFileResource bufferedFileResource, byte[] target, String expectedStream) throws IOException {
        InputStream inputStream = bufferedFileResource.createStream();
        inputStream.read(target);
        assertThat(new String(target)).isEqualTo(expectedStream);
        inputStream.close();
    }

    private FileResource getFileResource(final String stream) {
        return new FileResource() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public InputStream createStream() throws IOException {
                return new ByteArrayInputStream(stream.getBytes());
            }

            @Override
            public File getFile() throws IOException {
                return null;
            }
        };
    }

}
