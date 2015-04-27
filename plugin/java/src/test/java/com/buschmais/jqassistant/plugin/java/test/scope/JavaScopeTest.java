package com.buschmais.jqassistant.plugin.java.test.scope;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.ArtifactBasedTypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JavaScopeTest {

    @Mock private ScannerContext scannerContext;

    @Test
    public void useExistingTypeResolver() {
        TypeResolver typeResolver = mock(TypeResolver.class);
        when(scannerContext.peek(TypeResolver.class)).thenReturn(typeResolver);
        JavaScope.CLASSPATH.create(scannerContext);
        verify(scannerContext).peek(TypeResolver.class);
        verify(scannerContext).push(TypeResolver.class, typeResolver);
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
        verify(scannerContext).push(eq(TypeResolver.class), any(ArtifactBasedTypeResolver.class));
        JavaScope.CLASSPATH.destroy(scannerContext);
        verify(scannerContext).pop(TypeResolver.class);
    }

}
