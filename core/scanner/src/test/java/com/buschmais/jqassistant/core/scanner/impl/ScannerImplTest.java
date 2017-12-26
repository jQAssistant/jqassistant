package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ScannerImplTest {

    private ScannerConfiguration configuration = new ScannerConfiguration();

    @Test
    public void acceptReturnTrueIfPluginAcceptsResource() throws IOException {
        ScannerContext context = mock(ScannerContext.class);
        Map<String, ScannerPlugin<?, ?>> plugins = Collections.emptyMap();
        Map<String, Scope> scopes = Collections.emptyMap();

        ScannerImpl scanner = new ScannerImpl(configuration, context, plugins, scopes);

        Properties resource = mock(Properties.class);
        String path = "/a/b/c.properties";
        Scope scope = mock(Scope.class);

        ScannerPlugin<Properties, ?> selectedPlugin = mock(ScannerPlugin.class);

        doReturn(Boolean.TRUE).when(selectedPlugin).accepts(Mockito.<Properties>anyObject(), Mockito.eq(path), Mockito.eq(scope));

        boolean result = scanner.accepts(selectedPlugin, resource, path, scope);

        assertThat(result, is(true));
    }

    @Test
    public void acceptReturnFalseIfPluginRefusesResource() throws IOException {
        ScannerContext context = mock(ScannerContext.class);
        Map<String, ScannerPlugin<?, ?>> plugins = Collections.emptyMap();
        Map<String, Scope> scopes = Collections.emptyMap();

        ScannerImpl scanner = new ScannerImpl(configuration, context, plugins, scopes);

        Properties resource = mock(Properties.class);
        String path = "/a/b/c.properties";
        Scope scope = mock(Scope.class);

        ScannerPlugin<Properties, ?> selectedPlugin = mock(ScannerPlugin.class);

        doReturn(Boolean.FALSE).when(selectedPlugin).accepts(Mockito.<Properties>anyObject(), Mockito.eq(path), Mockito.eq(scope));

        boolean result = scanner.accepts(selectedPlugin, resource, path, scope);

        assertThat(result, is(false));
    }

    @Test
    public void continueOnError() throws IOException {
        ScannerContext context = mock(ScannerContext.class);
        ScannerPlugin<String, Descriptor> scannerPlugin = mock(ScannerPlugin.class);
        Map<String, ScannerPlugin<?, ?>> plugins = new HashMap<>();
        plugins.put("mock", scannerPlugin);
        Map<String, Scope> scopes = Collections.emptyMap();
        Scope scope = mock(Scope.class);

        Scanner scanner = new ScannerImpl(configuration, context, plugins, scopes);

        doReturn(String.class).when(scannerPlugin).getType();
        when(scannerPlugin.accepts("test", "test", scope)).thenReturn(true);
        when(scannerPlugin.scan("test", "test", scope, scanner)).thenThrow(new IllegalStateException("Test"));

        configuration.setContinueOnError(true);
        scanner.scan("test", "test", scope);

        configuration.setContinueOnError(false);
        try {
            scanner.scan("test", "test", scope);
            fail("Expecting an " + IllegalStateException.class.getName());
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            assertThat(message, containsString("test"));
        }
    }
}
