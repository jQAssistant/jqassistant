package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.TarArchiveDescriptor;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.common.test.assertj.FileDescriptorCondition.fileDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies scanning of TAR arichves.
 */
class TarScannerIT extends com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT {

    @Test
    void tarFile() throws Exception {
        test(FileResourceStrategy.File);
    }

    @Test
    void tarUrl() throws Exception {
        test(FileResourceStrategy.Url);
    }

    private void test(FileResourceStrategy strategy) throws Exception {
        File archive = createTarArchive();
        try {
            store.beginTransaction();
            FileDescriptor descriptor = getScanner().scan(strategy.get(archive), archive.getAbsolutePath(), DefaultScope.NONE);
            assertThat(descriptor).isInstanceOf(TarArchiveDescriptor.class);
            TarArchiveDescriptor archiveDescriptor = (TarArchiveDescriptor) descriptor;
            assertThat(archiveDescriptor.getContains()
                .size()).isEqualTo(2);
            assertThat(archiveDescriptor.getContains()).haveAtLeastOne(fileDescriptor("/test1.txt"));
            assertThat(archiveDescriptor.getContains()).haveAtLeastOne(fileDescriptor("/test2.txt"));
            store.commitTransaction();
        } finally {
            archive.delete();
        }
    }

    /**
     * Creates a TAR archive.
     *
     * @return archive The archive.
     * @throws IOException
     */
    private File createTarArchive() throws IOException {
        File archive = File.createTempFile("test", ".tar");
        TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(new FileOutputStream(archive));
        addEntry(tarOutputStream, "test1.txt", "Foo");
        addEntry(tarOutputStream, "test2.txt", "Bar");
        tarOutputStream.finish();
        return archive;
    }

    private void addEntry(TarArchiveOutputStream tarOutputStream, String fileName, String content) throws IOException {
        TarArchiveEntry fileEntry = new TarArchiveEntry(fileName);
        fileEntry.setSize(content.getBytes().length);
        tarOutputStream.putArchiveEntry(fileEntry);
        tarOutputStream.write(content.getBytes());
        tarOutputStream.closeArchiveEntry();
    }
}
