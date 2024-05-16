package com.buschmais.jqassistant.core.scanner.impl;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ScannerImplTest {

    private static final File WORKING_DIRECTORY = new File(".");

    private static final File OUTPUT_DIRECTORY = new File(WORKING_DIRECTORY, "jqassistant");

    @Mock
    private Store store;

    @Mock
    private ScannerPlugin<String, ?> scannerPlugin;

    @Mock
    private Scope scope;

    @Mock
    private ScannerPluginRepository scannerPluginRepository;

    @Mock
    private Scan configuration;

    private ScannerContext context;

    private boolean transaction = false;

    @BeforeEach
    void setup() throws IOException {
        // Plugin
        doReturn(String.class).when(scannerPlugin)
            .getType();
        when(scannerPlugin.accepts(anyString(), anyString(), eq(scope))).thenReturn(true);
        doAnswer(invocation -> {
            assertThat(transaction).isTrue();
            return mock(Descriptor.class);
        }).when(scannerPlugin)
            .scan(anyString(), anyString(), any(Scope.class), any(Scanner.class));
        // Store
        doAnswer(invocation -> transaction).when(store)
            .hasActiveTransaction();
        doAnswer(invocation -> {
            transaction = true;
            return null;
        }).when(store)
            .beginTransaction();
        doAnswer(invocation -> {
            transaction = false;
            return null;
        }).when(store)
            .commitTransaction();
        doAnswer(invocation -> {
            transaction = false;
            return null;
        }).when(store)
            .rollbackTransaction();
        // context
        context = new ScannerContextImpl(ScannerImplTest.class.getClassLoader(), store, WORKING_DIRECTORY, OUTPUT_DIRECTORY);
        doReturn(Set.<ScannerPlugin<?, ?>>of(scannerPlugin)).when(scannerPluginRepository)
            .getScannerPlugins(configuration, context);
    }

    @Test
    void resolveScope() {
        Scanner scanner = new ScannerImpl(configuration, context, scannerPluginRepository);
        assertThat(scanner.resolveScope("default:none")).isEqualTo(DefaultScope.NONE);
        assertThat(scanner.resolveScope("unknown")).isEqualTo(DefaultScope.NONE);
        assertThat(scanner.resolveScope(null)).isEqualTo(DefaultScope.NONE);
    }

    @Test
    void acceptReturnTrueIfPluginAcceptsResource() throws IOException {
        Properties resource = mock(Properties.class);
        String path = "/a/b/c.properties";
        ScannerPlugin<Properties, ?> selectedPlugin = mock(ScannerPlugin.class);
        doReturn(Boolean.TRUE).when(selectedPlugin)
            .accepts(Mockito.<Properties>anyObject(), Mockito.eq(path), Mockito.eq(scope));
        ScannerImpl scanner = new ScannerImpl(configuration, context, scannerPluginRepository);

        boolean result = scanner.accepts(selectedPlugin, resource, path, scope);

        assertThat(result).isTrue();
    }

    @Test
    void acceptReturnFalseIfPluginRefusesResource() throws IOException {
        Properties resource = mock(Properties.class);
        String path = "/a/b/c.properties";
        ScannerPlugin<Properties, ?> selectedPlugin = mock(ScannerPlugin.class);
        doReturn(Boolean.FALSE).when(selectedPlugin)
            .accepts(Mockito.anyObject(), Mockito.eq(path), Mockito.eq(scope));
        ScannerImpl scanner = new ScannerImpl(configuration, context, scannerPluginRepository);

        boolean result = scanner.accepts(selectedPlugin, resource, path, scope);

        assertThat(result).isFalse();
    }

    @Test
    void failOnError() throws IOException {
        Scanner scanner = new ScannerImpl(configuration, context, scannerPluginRepository);
        stubExceptionDuringScan(scanner);
        try {
            scanner.scan("test", "test", scope);
            fail("Expecting an " + UnrecoverableScannerException.class.getName());
        } catch (UnrecoverableScannerException e) {
            String message = e.getMessage();
            assertThat(message).contains("test");
        }

        verify(store).beginTransaction();
        verify(store).rollbackTransaction();
        verify(store, never()).commitTransaction();
        assertThat(transaction).isFalse();
    }

    @Test
    void continueOnError() throws IOException {
        doReturn(true).when(configuration)
            .continueOnError();
        Scanner scanner = new ScannerImpl(configuration, context, scannerPluginRepository);
        stubExceptionDuringScan(scanner);

        scanner.scan("test", "test", scope);
        scanner.scan("test", "test", scope);

        verify(store, times(2)).beginTransaction();
        verify(store, times(2)).rollbackTransaction();
        verify(store, never()).commitTransaction();
    }

    private void stubExceptionDuringScan(Scanner scanner) throws IOException {
        doAnswer(invocation -> {
            assertThat(transaction).isTrue();
            throw new IllegalStateException("Exception in plugin");
        }).when(scannerPlugin)
            .scan("test", "test", scope, scanner);
    }

    @Test
    void continueOnErrorDuringCommit() {
        doThrow(new IllegalStateException("Exception during commit")).when(store)
            .commitTransaction();
        doReturn(true).when(configuration)
            .continueOnError();
        Scanner scanner = new ScannerImpl(configuration, context, scannerPluginRepository);

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
    void pluginPipeline() {
        Store store = mock(Store.class);
        ScannerContext scannerContext = new ScannerContextImpl(ScannerImplTest.class.getClassLoader(), store, WORKING_DIRECTORY, OUTPUT_DIRECTORY);
        when(store.create(any(Class.class))).thenAnswer((Answer<Descriptor>) invocation -> {
            Class<? extends Descriptor> descriptorType = (Class<? extends Descriptor>) invocation.getArguments()[0];
            return mock(descriptorType);
        });
        when(store.addDescriptorType(any(Descriptor.class), any(Class.class))).thenAnswer((Answer<Descriptor>) invocation -> {
            Class<? extends Descriptor> descriptorType = (Class<? extends Descriptor>) invocation.getArguments()[1];
            return mock(descriptorType);
        });
        doReturn(Set.<ScannerPlugin<?, ?>>of(new TestItemScannerPlugin(), new DependentTestItemScannerPlugin(), new NestedTestItemScannerPlugin())).when(
                scannerPluginRepository)
            .getScannerPlugins(configuration, scannerContext);
        Scanner scanner = new ScannerImpl(configuration, scannerContext, scannerPluginRepository);

        Descriptor descriptor = scanner.scan(new TestItem(), "/", DefaultScope.NONE);

        assertThat(descriptor).isInstanceOf(DependentTestItemDescriptor.class);
        verify(store).create(TestItemDescriptor.class);
        verify(store).addDescriptorType(any(TestItemDescriptor.class), eq(NestedTestItemDescriptor.class));
        verify(store).addDescriptorType(any(NestedTestItemDescriptor.class), eq(DependentTestItemDescriptor.class));
    }
}
