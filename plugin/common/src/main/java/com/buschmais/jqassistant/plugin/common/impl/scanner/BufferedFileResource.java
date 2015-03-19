package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Implementation of a file resource which wraps a buffered inputstream and
 * allows re-using a once opened stream.
 */
public class BufferedFileResource implements FileResource {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private BufferStream bufferStream = null;

    private int bufferSize;

    private FileResource fileResource;

    /**
     * Constructor.
     * 
     * @param fileResource
     *            The wrapped file resource.
     */
    public BufferedFileResource(FileResource fileResource) {
        this(fileResource, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param fileResource
     *            The wrapped file resource.
     * @param bufferSize
     *            The buffer size in bytes to use.
     */
    public BufferedFileResource(FileResource fileResource, int bufferSize) {
        this.fileResource = fileResource;
        this.bufferSize = bufferSize;
    }

    @Override
    public InputStream createStream() throws IOException {
        if (bufferStream == null || !bufferStream.isReUsable()) {
            bufferStream = new BufferStream(fileResource.createStream(), bufferSize);
        }
        return bufferStream;
    }

    @Override
    public File getFile() throws IOException {
        return fileResource.getFile();
    }

    @Override
    public void close() throws IOException {
        fileResource.close();
    }

    /**
     * Implementation of a re-usable buffered stream.
     */
    private static class BufferStream extends BufferedInputStream {

        /**
         * Constructor.
         * 
         * @param inputStream
         *            The wrapped inputstream.
         * @param bufferSize
         *            The buffer size to use.
         */
        public BufferStream(InputStream inputStream, int bufferSize) {
            super(inputStream, bufferSize);
            mark(bufferSize);
        }

        @Override
        public void close() throws IOException {
            if (isReUsable()) {
                reset();
            } else {
                in.close();
            }
        }

        /**
         * Return <code>true</code> if this stream instance can be re-used by
         * another request.
         * 
         * @return <code>true</code> if this stream instance can be re-used by
         *         another request.
         */
        boolean isReUsable() {
            return markpos != -1;
        }
    }
}
