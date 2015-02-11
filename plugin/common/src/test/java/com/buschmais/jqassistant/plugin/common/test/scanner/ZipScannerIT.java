package com.buschmais.jqassistant.plugin.common.test.scanner;

import static com.buschmais.jqassistant.plugin.common.test.matcher.FileDescriptorMatcher.fileDescriptorMatcher;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

/**
 * Verifies scanning of ZIP arichves.
 */
public class ZipScannerIT extends AbstractPluginIT {

    /**
     * The strategies for referencing the archive to scan.
     */
    enum Strategy {
        /**
         * As URL.
         */
        Url {
            Object get(File file) throws MalformedURLException {
                return file.toURI().toURL();
            }
        },
        /**
         * As file.
         */
        File {
            Object get(File file) {
                return file;
            }
        };

        abstract <T> T get(File file) throws Exception;
    }

    @Test
    public void zipFile() throws Exception {
        test(Strategy.File);
    }

    @Test
    public void zipUrl() throws Exception {
        test(Strategy.Url);
    }

    private void test(Strategy strategy) throws Exception {
        File archive = createZipArchive();
        store.beginTransaction();
        FileDescriptor descriptor = getScanner().scan(strategy.get(archive), archive.getAbsolutePath(), null);
        assertThat(descriptor, instanceOf(ArchiveDescriptor.class));
        ArchiveDescriptor archiveDescriptor = (ArchiveDescriptor) descriptor;
        assertThat(archiveDescriptor.getContains(), hasItem(fileDescriptorMatcher("/test.txt")));
        store.commitTransaction();
        archive.delete();
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
        ZipEntry fileEntry = new ZipEntry("test.txt");
        fileEntry.setTime(System.currentTimeMillis());
        zipOutputStream.putNextEntry(fileEntry);
        zipOutputStream.write("Hello World!".getBytes());
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        return archive;
    }
}
