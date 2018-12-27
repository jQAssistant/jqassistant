package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ScannerImplTest {

    @Mock
    private Store store;

    @Mock
    private ScannerPlugin<String, ?> scannerPlugin;

    @Mock
    private Scope scope;

    private ScannerContext context;

    private Map<String, ScannerPlugin<?, ?>> plugins = new HashMap<>();

    private boolean transaction = false;

    private ScannerConfiguration configuration = new ScannerConfiguration();

    @BeforeEach
    public void setup() throws IOException {
        // Plugin
        doReturn(String.class).when(scannerPlugin).getType();
        when(scannerPlugin.accepts(anyString(), anyString(), eq(scope))).thenReturn(true);
        doAnswer(invocation -> {
            assertThat(transaction, equalTo(true));
            return mock(Descriptor.class);
        }).when(scannerPlugin).scan(anyString(), anyString(), any(Scope.class), any(Scanner.class));
        plugins.put("testPlugin", scannerPlugin);
        // Store
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
        context = new ScannerContextImpl(store);
    }

    @Test
    public void resolveScope() {
        ScannerContext scannerContext = mock(ScannerContext.class);
        Scanner scanner = new ScannerImpl(new ScannerConfiguration(), scannerContext, emptyMap(), emptyMap());
        Assert.assertThat(scanner.resolveScope("default:none"), equalTo(DefaultScope.NONE));
        Assert.assertThat(scanner.resolveScope("unknown"), equalTo(DefaultScope.NONE));
        Assert.assertThat(scanner.resolveScope(null), equalTo(DefaultScope.NONE));
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
    public void failOnError() throws IOException {
        Scanner scanner = new ScannerImpl(configuration, context, plugins, emptyMap());
        stubExceptionDuringScan(scanner);
        try {
            scanner.scan("test", "test", scope);
            fail("Expecting an " + UnrecoverableScannerException.class.getName());
        } catch (UnrecoverableScannerException e) {
            String message = e.getMessage();
            assertThat(message, containsString("test"));
        }

        verify(store).beginTransaction();
        verify(store).rollbackTransaction();
        verify(store, never()).commitTransaction();
        assertThat(transaction, equalTo(false));
    }

    @Test
    public void continueOnError() throws IOException {
        Scanner scanner = new ScannerImpl(configuration, context, plugins, emptyMap());
        stubExceptionDuringScan(scanner);
        configuration.setContinueOnError(true);

        scanner.scan("test", "test", scope);
        scanner.scan("test", "test", scope);

        verify(store, times(2)).beginTransaction();
        verify(store, times(2)).rollbackTransaction();
        verify(store, never()).commitTransaction();
    }

    private void stubExceptionDuringScan(Scanner scanner) throws IOException {
        doAnswer(invocation -> {
            assertThat(transaction, equalTo(true));
            throw new IllegalStateException("Exception in plugin");
        }).when(scannerPlugin).scan("test", "test", scope, scanner);
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

    /**
     * Verifies correct execution of the pipeline for dependent and nested scanner
     * plugins:
     * 
     * <ul>
     * <li>A {@link TestItem} is scanned initially by
     * {@link TestItemScannerPlugin}.</li>
     * <li>The created {@link TestItemDescriptor} is then passed to the
     * {@link DependentTestItemScannerPlugin}.</li>
     * <li>The {@link DependentTestItemScannerPlugin} triggers a separate scan using
     * the {@link TestScope} which activates the
     * {@link NestedTestItemScannerPlugin}.</li>
     * </ul>
     */
    @Test
    public void pluginPipeline() {
        Store store = mock(Store.class);
        ScannerContext scannerContext = new ScannerContextImpl(store);
        when(store.create(any(Class.class))).thenAnswer((Answer<Descriptor>) invocation -> {
            Class<? extends Descriptor> descriptorType = (Class<? extends Descriptor>) invocation.getArguments()[0];
            return mock(descriptorType);
        });
        when(store.addDescriptorType(any(Descriptor.class), any(Class.class))).thenAnswer((Answer<Descriptor>) invocation -> {
            Class<? extends Descriptor> descriptorType = (Class<? extends Descriptor>) invocation.getArguments()[1];
            return mock(descriptorType);
        });
        Map<String, ScannerPlugin<?, ?>> scannerPlugins = new HashMap<>();
        scannerPlugins.put("TestScanner", new TestItemScannerPlugin());
        scannerPlugins.put("DependentTestScanner", new DependentTestItemScannerPlugin());
        scannerPlugins.put("NestedTestScanner", new NestedTestItemScannerPlugin());
        Scanner scanner = new ScannerImpl(new ScannerConfiguration(), scannerContext, scannerPlugins, Collections.<String, Scope> emptyMap());

        Descriptor descriptor = scanner.scan(new TestItem(), "/", DefaultScope.NONE);

        assertThat(descriptor, instanceOf(DependentTestItemDescriptor.class));
        verify(store).create(eq(TestItemDescriptor.class));
        verify(store).addDescriptorType(any(TestItemDescriptor.class), eq(NestedTestItemDescriptor.class));
        verify(store).addDescriptorType(any(NestedTestItemDescriptor.class), eq(DependentTestItemDescriptor.class));
    }
}
