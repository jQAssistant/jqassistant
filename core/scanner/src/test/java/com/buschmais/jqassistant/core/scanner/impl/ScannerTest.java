package com.buschmais.jqassistant.core.scanner.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.*;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

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

    @Test
    public void pluginPipeline() {
        ScannerContext scannerContext = mock(ScannerContext.class);
        Store store = mock(Store.class);
        when(scannerContext.getStore()).thenReturn(store);
        when(store.create(Mockito.any(Class.class))).thenAnswer(new Answer<Descriptor>() {
            @Override
            public Descriptor answer(InvocationOnMock invocation) throws Throwable {
                Class<? extends Descriptor> descriptorType = (Class<? extends Descriptor>) invocation.getArguments()[0];
                return mock(descriptorType);
            }
        });
        when(store.addDescriptorType(Mockito.any(Descriptor.class), Mockito.any(Class.class))).thenAnswer(new Answer<Descriptor>() {
            @Override
            public Descriptor answer(InvocationOnMock invocation) throws Throwable {
                Class<? extends Descriptor> descriptorType = (Class<? extends Descriptor>) invocation.getArguments()[1];
                return mock(descriptorType);
            }
        });
        List<ScannerPlugin<?, ?>> scannerPlugins = new ArrayList<>();
        scannerPlugins.add(new TestScannerPlugin1());
        scannerPlugins.add(new TestScannerPlugin2());
        scannerPlugins.add(new TestScannerPlugin2A());
        Scanner scanner = new ScannerImpl(scannerContext, scannerPlugins, Collections.<String, Scope> emptyMap());

        Descriptor descriptor = scanner.scan(new TestItem(), "/", DefaultScope.NONE);

        assertThat(descriptor, instanceOf(TestDescriptor2.class));
        verify(store).create(Mockito.eq(TestDescriptor1.class));
        verify(store).addDescriptorType(Mockito.any(TestDescriptor1.class), Mockito.eq(TestDescriptor2A.class));
        verify(store).addDescriptorType(Mockito.any(TestDescriptor2A.class), Mockito.eq(TestDescriptor2.class));
        verify(scannerContext).push(Mockito.eq(TestDescriptor1.class), Mockito.any(TestDescriptor1.class));
        verify(scannerContext).peek(TestDescriptor1.class);
        verify(scannerContext).pop(TestDescriptor1.class);
    }

}
