package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ScannerTest {

    @Test
    public void resolveScope() {
        ScannerContext scannerContext = mock(ScannerContext.class);
        Scanner scanner = new ScannerImpl(new ScannerConfiguration(), scannerContext, Collections.<String, ScannerPlugin<?, ?>>emptyMap(), Collections.<String, Scope>emptyMap());
        assertThat(scanner.resolveScope("default:none"), CoreMatchers.<Scope>equalTo(DefaultScope.NONE));
        assertThat(scanner.resolveScope("unknown"), CoreMatchers.<Scope>equalTo(DefaultScope.NONE));
        assertThat(scanner.resolveScope(null), CoreMatchers.<Scope>equalTo(DefaultScope.NONE));
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
        Map<String, ScannerPlugin<?, ?>> scannerPlugins = new HashMap<>();
        scannerPlugins.put("TestScanner1", new TestScannerPlugin1());
        scannerPlugins.put("TestScanner2", new TestScannerPlugin2());
        scannerPlugins.put("TestScanner2A", new TestScannerPlugin2A());
        Scanner scanner = new ScannerImpl(new ScannerConfiguration(), scannerContext, scannerPlugins, Collections.<String, Scope>emptyMap());

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
