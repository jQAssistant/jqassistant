package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScannerImplTest {

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    @Mock
    private ScannerPlugin<String, ?> scannerPlugin;

    @Mock
    private Scope scope;

    private Map<String, ScannerPlugin<?, ?>> plugins = new HashMap<>();

    private boolean transaction = false;

    private ScannerConfiguration configuration = new ScannerConfiguration();

    @Before
    public void setup() throws IOException {
        // Plugin
        doReturn(String.class).when(scannerPlugin).getType();
        when(scannerPlugin.accepts(anyString(), anyString(), eq(scope))).thenReturn(true);
        plugins.put("testPlugin", scannerPlugin);
        // Store
        doReturn(store).when(context).getStore();
        doAnswer(invocation -> transaction).when(store).hasActiveTransaction();
        doAnswer(invocation -> {
            transaction = true;
            return null;
        }).when(store).beginTransaction();
        doAnswer(invocation -> {
            transaction = false;
            return null;
        }).when(store).commitTransaction();
        doAnswer(invocation -> {
            transaction = false;
            return null;
        }).when(store).rollbackTransaction();
    }

    @Test
    public void acceptReturnTrueIfPluginAcceptsResource() throws IOException {
        Properties resource = mock(Properties.class);
        String path = "/a/b/c.properties";
        ScannerPlugin<Properties, ?> selectedPlugin = mock(ScannerPlugin.class);
        doReturn(Boolean.TRUE).when(selectedPlugin).accepts(Mockito.<Properties> anyObject(), Mockito.eq(path), Mockito.eq(scope));
        ScannerImpl scanner = new ScannerImpl(configuration, context, plugins, emptyMap());

        boolean result = scanner.accepts(selectedPlugin, resource, path, scope);

        assertThat(result, is(true));
    }

    @Test
    public void acceptReturnFalseIfPluginRefusesResource() throws IOException {
        Properties resource = mock(Properties.class);
        String path = "/a/b/c.properties";
        ScannerPlugin<Properties, ?> selectedPlugin = mock(ScannerPlugin.class);
        doReturn(Boolean.FALSE).when(selectedPlugin).accepts(Mockito.anyObject(), Mockito.eq(path), Mockito.eq(scope));
        ScannerImpl scanner = new ScannerImpl(configuration, context, plugins, emptyMap());

        boolean result = scanner.accepts(selectedPlugin, resource, path, scope);

        assertThat(result, is(false));
    }

    @Test
    public void continueOnError() throws IOException {
        Scanner scanner = new ScannerImpl(configuration, context, plugins, emptyMap());
        when(scannerPlugin.scan("test", "test", scope, scanner)).thenThrow(new IllegalStateException("Exception in plugin"));

        configuration.setContinueOnError(true);
        scanner.scan("test", "test", scope);

        verify(store).beginTransaction();
        verify(store).rollbackTransaction();
        verify(store, never()).commitTransaction();

        configuration.setContinueOnError(false);
        try {
            scanner.scan("test", "test", scope);
            fail("Expecting an " + IllegalStateException.class.getName());
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            assertThat(message, containsString("test"));
        }
    }

    @Test
    public void continueOnErrorDuringCommit() {
        doThrow(new IllegalStateException("Exception during commit")).when(store).commitTransaction();
        configuration.setContinueOnError(true);
        Scanner scanner = new ScannerImpl(configuration, context, plugins, emptyMap());

        scanner.scan("test1", "test1", scope);
        scanner.scan("test2", "test2", scope);

        verify(store, times(2)).beginTransaction();
        verify(store, times(2)).commitTransaction();
        verify(store, times(2)).rollbackTransaction();
    }
}
