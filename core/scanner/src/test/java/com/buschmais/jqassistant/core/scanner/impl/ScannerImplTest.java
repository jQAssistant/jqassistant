package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

public class ScannerImplTest {

    @Test
    public void acceptReturnTrueIfPluginAcceptsResource() throws IOException {
        ScannerContext context = Mockito.mock(ScannerContext.class);
        List<ScannerPlugin<?, ?>> plugins = Collections.emptyList();
        Map<String, Scope> scopes = Collections.emptyMap();

        ScannerImpl scanner = new ScannerImpl(context, plugins, scopes);

        Properties resource = Mockito.mock(Properties.class);
        String path = "/a/b/c.properties";
        Scope scope = Mockito.mock(Scope.class);

        ScannerPlugin<Properties, ?> selectedPlugin = Mockito.mock(ScannerPlugin.class);

        doReturn(Boolean.TRUE).when(selectedPlugin).accepts(Mockito.<Properties>anyObject(), Mockito.eq(path), Mockito.eq(scope));

        boolean result = scanner.accepts(selectedPlugin, resource, path, scope);

        assertThat(result, is(true));
    }

    @Test
    public void acceptReturnFalseIfPluginRefusesResource() throws IOException {
        ScannerContext context = Mockito.mock(ScannerContext.class);
        List<ScannerPlugin<?, ?>> plugins = Collections.emptyList();
        Map<String, Scope> scopes = Collections.emptyMap();

        ScannerImpl scanner = new ScannerImpl(context, plugins, scopes);

        Properties resource = Mockito.mock(Properties.class);
        String path = "/a/b/c.properties";
        Scope scope = Mockito.mock(Scope.class);

        ScannerPlugin<Properties, ?> selectedPlugin = Mockito.mock(ScannerPlugin.class);

        doReturn(Boolean.FALSE).when(selectedPlugin).accepts(Mockito.<Properties>anyObject(), Mockito.eq(path), Mockito.eq(scope));

        boolean result = scanner.accepts(selectedPlugin, resource, path, scope);

        assertThat(result, is(false));
    }

}