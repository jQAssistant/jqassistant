package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ZipArchiveDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.common.test.matcher.FileDescriptorMatcher.fileDescriptorMatcher;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Verifies scanning of ZIP arichves.
 */
class ZipScannerIT extends com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT {

    @Test
    void zipFile() throws Exception {
        test(FileResourceStrategy.File);
    }

    @Test
    void zipUrl() throws Exception {
        test(FileResourceStrategy.Url);
    }

    @Test
    void invalidZip() throws Exception {
        File archive = File.createTempFile("test", ".zip");
        archive.deleteOnExit();
        store.beginTransaction();
        FileDescriptor descriptor = getScanner().scan(archive, archive.getAbsolutePath(), null);
        assertThat(descriptor, instanceOf(ZipArchiveDescriptor.class));
        ZipArchiveDescriptor zipArchiveDescriptor = (ZipArchiveDescriptor) descriptor;
        assertThat(zipArchiveDescriptor.isValid(), equalTo(false));
        store.commitTransaction();
    }

    private void test(FileResourceStrategy strategy) throws Exception {
        File archive = createZipArchive();
        try {
            store.beginTransaction();
            FileDescriptor descriptor = getScanner().scan(strategy.get(archive), archive.getAbsolutePath(), DefaultScope.NONE);
            assertThat(descriptor, instanceOf(ZipArchiveDescriptor.class));
            ZipArchiveDescriptor archiveDescriptor = (ZipArchiveDescriptor) descriptor;
            assertThat(archiveDescriptor.isValid(), equalTo(true));
            assertThat(archiveDescriptor.getContains(), hasItem(fileDescriptorMatcher("/test1.txt")));
            assertThat(archiveDescriptor.getContains(), hasItem(fileDescriptorMatcher("/test2.txt")));
            store.commitTransaction();
        } finally {
            archive.delete();
        }
    }

    /**
     * Creates a ZIP archvie containing a file "test.txt"
     *
     * @return archive The archive.
     * @throws IOException
     */
    private File createZipArchive() throws IOException {
        File archive = File.createTempFile("test", ".zip");
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(archive));
        addEntry(zipOutputStream, "test1.txt", "Foo");
        addEntry(zipOutputStream, "test2.txt", "Bar");
        zipOutputStream.close();
        return archive;
    }

    private void addEntry(ZipOutputStream zipOutputStream, String fileName, String content) throws IOException {
        ZipEntry fileEntry = new ZipEntry(fileName);
        fileEntry.setTime(System.currentTimeMillis());
        zipOutputStream.putNextEntry(fileEntry);
        zipOutputStream.write(content.getBytes());
        zipOutputStream.closeEntry();
    }
}
