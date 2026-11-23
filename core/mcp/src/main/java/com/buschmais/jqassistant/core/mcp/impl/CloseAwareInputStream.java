package com.buschmais.jqassistant.core.mcp.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link InputStream} notifying a listener once if the end of the stream has been reached, the stream has been closed or reading failed.
 * <p>
 * Used to detect that the MCP client closed the connection.
 */
class CloseAwareInputStream extends FilterInputStream {

    private final Runnable closeListener;

    private final AtomicBoolean notified = new AtomicBoolean();

    CloseAwareInputStream(InputStream in, Runnable closeListener) {
        super(in);
        this.closeListener = closeListener;
    }

    @Override
    public int read() throws IOException {
        return notifyOnEnd(super::read);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return notifyOnEnd(() -> super.read(b, off, len));
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            notifyClosed();
        }
    }

    private int notifyOnEnd(ReadOperation readOperation) throws IOException {
        try {
            int result = readOperation.read();
            if (result < 0) {
                notifyClosed();
            }
            return result;
        } catch (IOException e) {
            notifyClosed();
            throw e;
        }
    }

    private void notifyClosed() {
        if (notified.compareAndSet(false, true)) {
            closeListener.run();
        }
    }

    @FunctionalInterface
    private interface ReadOperation {
        int read() throws IOException;
    }
}
