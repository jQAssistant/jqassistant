package com.buschmais.jqassistant.plugin.common.test.scanner;

import static com.buschmais.jqassistant.plugin.common.test.matcher.FileDescriptorMatcher.fileDescriptorMatcher;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.TarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

/**
 * Verifies scanning of TAR arichves.
 */
public class TarScannerIT extends AbstractPluginIT {

    @Test
    public void zipFile() throws Exception {
        test(FileResourceStrategy.File);
    }

    @Test
    public void zipUrl() throws Exception {
        test(FileResourceStrategy.Url);
    }

    private void test(FileResourceStrategy strategy) throws Exception {
        File archive = createTarArchive();
        try {
            store.beginTransaction();
            FileDescriptor descriptor = getScanner().scan(strategy.get(archive), archive.getAbsolutePath(), null);
            assertThat(descriptor, instanceOf(TarArchiveDescriptor.class));
            TarArchiveDescriptor archiveDescriptor = (TarArchiveDescriptor) descriptor;
            assertThat(archiveDescriptor.getContains().size(), equalTo(2));
            assertThat(archiveDescriptor.getContains(), hasItem(fileDescriptorMatcher("/test1.txt")));
            assertThat(archiveDescriptor.getContains(), hasItem(fileDescriptorMatcher("/test2.txt")));
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
