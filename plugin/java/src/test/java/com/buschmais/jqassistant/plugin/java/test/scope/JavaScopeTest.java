package com.buschmais.jqassistant.plugin.java.test.scope;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.*;

@RunWith(MockitoJUnitRunner.class)
public class JavaScopeTest {

    @Mock
    private ScannerContext scannerContext;

    @Before
    public void setUp() {
        when(scannerContext.peek(FileResolver.class)).thenReturn(new FileResolver());
    }

    @Test
    public void useExistingTypeResolver() {
        TypeResolver typeResolver = mock(TypeResolver.class);
        when(scannerContext.peekOrDefault(TypeResolver.class, null)).thenReturn(typeResolver);
        JavaScope.CLASSPATH.create(scannerContext);
        verify(scannerContext).peekOrDefault(TypeResolver.class, null);
        verify(scannerContext).push(eq(TypeResolver.class), any(DelegatingTypeResolver.class));
        JavaScope.CLASSPATH.destroy(scannerContext);
        verify(scannerContext).pop(TypeResolver.class);
    }

    @Test
    public void createArtifactTypeResolver() {
        when(scannerContext.peekOrDefault(TypeResolver.class, null)).thenReturn(null);
        JavaArtifactFileDescriptor artifactFileDescriptor = mock(JavaArtifactFileDescriptor.class);
        when(scannerContext.peekOrDefault(JavaArtifactFileDescriptor.class, null)).thenReturn(artifactFileDescriptor);
        JavaScope.CLASSPATH.create(scannerContext);
        verify(scannerContext).peekOrDefault(TypeResolver.class, null);
        verify(scannerContext).push(eq(TypeResolver.class), any(ClasspathScopedTypeResolver.class));
        JavaScope.CLASSPATH.destroy(scannerContext);
        verify(scannerContext).pop(TypeResolver.class);
    }

    @Test
    public void createDefaultTypeResolver() {
        when(scannerContext.peekOrDefault(TypeResolver.class, null)).thenReturn(null);
        when(scannerContext.peekOrDefault(JavaArtifactFileDescriptor.class, null)).thenReturn(null);
        JavaScope.CLASSPATH.create(scannerContext);
        verify(scannerContext).peekOrDefault(TypeResolver.class, null);
        verify(scannerContext).push(eq(TypeResolver.class), any(DefaultTypeResolver.class));
        JavaScope.CLASSPATH.destroy(scannerContext);
        verify(scannerContext).pop(TypeResolver.class);
    }
}
