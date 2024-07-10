package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractVirtualFileResourceTest {

    @Test
    void fileResource() throws IOException {
        String path = "/directory/file";
        String fileContent = "Hello World";
        File file;
        try (AbstractVirtualFileResource fileResource = new AbstractVirtualFileResource() {
            @Override
            public InputStream createStream() {
                return new ByteArrayInputStream(fileContent.getBytes(UTF_8));
            }

            @Override
            protected String getRelativePath() {
                return path;
            }
        }) {
            file = fileResource.getFile();
            assertThat(file).exists();
            assertThat(file).content(UTF_8)
                .isEqualTo(fileContent);
        }
        assertThat(file.toURI()
            .getPath()).endsWith(path);
        assertThat(file).doesNotExist();
    }

}
