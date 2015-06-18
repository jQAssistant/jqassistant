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
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolverProvider;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.*;

@RunWith(MockitoJUnitRunner.class)
public class JavaScopeTest {

    @Mock
    private ScannerContext scannerContext;

    @Before
    public void setUp() {
        when(scannerContext.peek(FileResolverProvider.class)).thenReturn(new FileResolverProvider());
    }

    @Test
    public void useExistingTypeResolver() {
        TypeResolver typeResolver = mock(TypeResolver.class);
        when(scannerContext.peek(TypeResolver.class)).thenReturn(typeResolver);
        JavaScope.CLASSPATH.create(scannerContext);
        verify(scannerContext).peek(TypeResolver.class);
        verify(scannerContext).push(eq(TypeResolver.class), any(DelegatingTypeResolver.class));
        JavaScope.CLASSPATH.destroy(scannerContext);
        verify(scannerContext).pop(TypeResolver.class);
    }

    @Test
    public void createArtifactTypeResolver() {
        when(scannerContext.peek(TypeResolver.class)).thenReturn(null);
        JavaArtifactFileDescriptor artifactFileDescriptor = mock(JavaArtifactFileDescriptor.class);
        when(scannerContext.peek(JavaArtifactFileDescriptor.class)).thenReturn(artifactFileDescriptor);
        JavaScope.CLASSPATH.create(scannerContext);
        verify(scannerContext).peek(TypeResolver.class);
        verify(scannerContext).push(eq(TypeResolver.class), any(ClasspathScopedTypeResolver.class));
        JavaScope.CLASSPATH.destroy(scannerContext);
        verify(scannerContext).pop(TypeResolver.class);
    }

    @Test
    public void createDefaultTypeResolver() {
        when(scannerContext.peek(TypeResolver.class)).thenReturn(null);
        when(scannerContext.peek(JavaArtifactFileDescriptor.class)).thenReturn(null);
        JavaScope.CLASSPATH.create(scannerContext);
        verify(scannerContext).peek(TypeResolver.class);
        verify(scannerContext).push(eq(TypeResolver.class), any(DefaultTypeResolver.class));
        JavaScope.CLASSPATH.destroy(scannerContext);
        verify(scannerContext).pop(TypeResolver.class);
    }
}
