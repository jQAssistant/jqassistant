package com.buschmais.jqassistant.plugin.common.test.scanner;

import static com.buschmais.jqassistant.plugin.common.test.matcher.FileDescriptorMatcher.fileDescriptorMatcher;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class ZipScannerIT extends AbstractPluginIT {

    @Test
    public void zipArchive() throws IOException {
        File archive = File.createTempFile("test", ".zip");
        createZipArchive(archive);
        store.beginTransaction();
        FileDescriptor descriptor = getScanner().scan(archive, archive.getAbsolutePath(), null);
        assertThat(descriptor, instanceOf(ArchiveDescriptor.class));
        ArchiveDescriptor archiveDescriptor = (ArchiveDescriptor) descriptor;
        assertThat(archiveDescriptor.getContains(), hasItem(fileDescriptorMatcher("/test.txt")));
        store.commitTransaction();
        archive.delete();
    }

    private void createZipArchive(File archive) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(archive));
        ZipEntry fileEntry = new ZipEntry("test.txt");
        fileEntry.setTime(System.currentTimeMillis());
        zipOutputStream.putNextEntry(fileEntry);
        zipOutputStream.write("Hello World!".getBytes());
        zipOutputStream.closeEntry();
        zipOutputStream.close();
    }
}
