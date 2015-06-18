package com.buschmais.jqassistant.plugin.common.test.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.plugin.common.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.GZipFileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

/**
 * Verifies scanning of gzipped files.
 */
public class GZipFileScannerIT extends AbstractPluginIT {

    /**
     * Scan a GZipped zip file.
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void gzippedTextFile() throws IOException {
        store.beginTransaction();
        File gzFile = File.createTempFile("test", ".txt.gz");
        gzFile.deleteOnExit();

        FileOutputStream outputStream = new FileOutputStream(gzFile);
        outputStream.write("Hello World".getBytes());
        outputStream.close();

        FileDescriptor descriptor = getScanner().scan(gzFile, gzFile.getAbsolutePath(), DefaultScope.NONE);
        assertThat("Expecting a GZIP descriptor.", descriptor, instanceOf(GZipFileDescriptor.class));
        String expectedGZFileName = gzFile.getAbsolutePath().replace('\\', '/');
        assertThat("Expecting an valid valid file name.", descriptor.getFileName(), equalTo(expectedGZFileName));
        GZipFileDescriptor gZipFileDescriptor = (GZipFileDescriptor) descriptor;
        assertThat("Expecting one entry.", gZipFileDescriptor.getContains().size(), equalTo(1));
        FileDescriptor fileDescriptor = gZipFileDescriptor.getContains().get(0);
        assertThat("Expecting a valid entry file name, e.g. wihout .gz", fileDescriptor.getFileName(),
                equalTo(expectedGZFileName.substring(0, expectedGZFileName.length() - 3)));
        store.commitTransaction();
    }

    /**
     * Scan a GZipped zip file.
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void gzippedZipFile() throws IOException {
        store.beginTransaction();
        File zipFile = File.createTempFile("test", ".zip");
        zipFile.deleteOnExit();
        File gzFile = File.createTempFile("test", ".zip.gz");
        gzFile.deleteOnExit();

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOutputStream.close();
        GZIPOutputStream os = new GZIPOutputStream(new FileOutputStream(gzFile));
        FileInputStream in = new FileInputStream(zipFile);
        IOUtils.copy(in, os);
        in.close();
        os.close();

        FileDescriptor descriptor = getScanner().scan(gzFile, gzFile.getAbsolutePath(), DefaultScope.NONE);
        assertThat(descriptor, instanceOf(GZipFileDescriptor.class));
        assertThat(descriptor.getFileName(), equalTo(gzFile.getAbsolutePath().replace('\\', '/')));
        GZipFileDescriptor gZipFileDescriptor = (GZipFileDescriptor) descriptor;
        assertThat(gZipFileDescriptor.getContains().size(), equalTo(1));
        FileDescriptor fileDescriptor = gZipFileDescriptor.getContains().get(0);
        assertThat(fileDescriptor, instanceOf(ArchiveDescriptor.class));
        store.commitTransaction();
    }

}
