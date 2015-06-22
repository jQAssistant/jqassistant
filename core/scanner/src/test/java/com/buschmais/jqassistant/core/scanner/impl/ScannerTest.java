package com.buschmais.jqassistant.core.scanner.impl;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;

public class ScannerTest {

    @Test
    public void resolveScope() {
        ScannerContext scannerContext = mock(ScannerContext.class);
        Map<String, Scope> scopes = new HashMap<>();
        scopes.put("default:none", DefaultScope.NONE);
        Scanner scanner = new ScannerImpl(scannerContext, Collections.<ScannerPlugin<?, ?>> emptyList(), Collections.<String, Scope> emptyMap());
        assertThat(scanner.resolveScope("default:none"), CoreMatchers.<Scope> equalTo(DefaultScope.NONE));
        assertThat(scanner.resolveScope("unknown"), CoreMatchers.<Scope> equalTo(DefaultScope.NONE));
        assertThat(scanner.resolveScope(null), CoreMatchers.<Scope> equalTo(DefaultScope.NONE));
    }

}
