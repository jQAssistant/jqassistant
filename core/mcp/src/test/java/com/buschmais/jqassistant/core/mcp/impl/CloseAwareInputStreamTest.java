package com.buschmais.jqassistant.core.mcp.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the {@link CloseAwareInputStream}.
 */
class CloseAwareInputStreamTest {

    private final AtomicInteger notifications = new AtomicInteger();

    private CloseAwareInputStream closeAwareInputStream;

    @BeforeEach
    void setUp() {
        closeAwareInputStream = new CloseAwareInputStream(new ByteArrayInputStream("ab".getBytes(UTF_8)), notifications::incrementAndGet);
    }

    @Test
    void notifiesOnEndOfStreamBySingleByteRead() throws IOException {
        assertThat(closeAwareInputStream.read()).isEqualTo('a');
        assertThat(closeAwareInputStream.read()).isEqualTo('b');
        assertThat(notifications).hasValue(0);

        assertThat(closeAwareInputStream.read()).isEqualTo(-1);

        assertThat(notifications).hasValue(1);
    }

    @Test
    void notifiesOnEndOfStreamByBufferRead() throws IOException {
        byte[] buffer = new byte[10];
        assertThat(closeAwareInputStream.read(buffer)).isEqualTo(2);
        assertThat(notifications).hasValue(0);

        assertThat(closeAwareInputStream.read(buffer)).isEqualTo(-1);

        assertThat(notifications).hasValue(1);
    }

    @Test
    void notifiesOnClose() throws IOException {
        closeAwareInputStream.close();

        assertThat(notifications).hasValue(1);
    }

    @Test
    void notifiesOnReadFailure() throws IOException {
        InputStream failingInputStream = mock(InputStream.class);
        when(failingInputStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Broken pipe"));
        CloseAwareInputStream failingCloseAwareInputStream = new CloseAwareInputStream(failingInputStream, notifications::incrementAndGet);

        assertThatThrownBy(() -> failingCloseAwareInputStream.read(new byte[10])).isInstanceOf(IOException.class);

        assertThat(notifications).hasValue(1);
    }

    @Test
    void notifiesOnlyOnce() throws IOException {
        closeAwareInputStream.readAllBytes();
        assertThat(closeAwareInputStream.read()).isEqualTo(-1);
        closeAwareInputStream.close();

        assertThat(notifications).hasValue(1);
    }
}
