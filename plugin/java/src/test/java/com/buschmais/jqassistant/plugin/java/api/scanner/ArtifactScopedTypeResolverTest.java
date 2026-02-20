package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.util.Collections;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtifactScopedTypeResolverTest {

    @Mock
    private JavaArtifactFileDescriptor artifact;

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private FileResolver fileResolver;

    private ArtifactScopedTypeResolver resolver;

    @BeforeEach
    void setUp() {
        when(artifact.getNumberOfDependencies()).thenReturn(0L);
        when(artifact.getContains()).thenReturn(Collections.emptyList());
        when(artifact.getRequires()).thenReturn(Collections.emptyList());
        resolver = new ArtifactScopedTypeResolver(artifact);
    }

    @Test
    void resolveReturnsNullWhenRequireFails() {
        when(scannerContext.peek(FileResolver.class)).thenReturn(fileResolver);
        when(fileResolver.require(anyString(), anyString(), eq(ClassFileDescriptor.class), eq(scannerContext))).thenReturn(null);

        TypeCache.CachedType<TypeDescriptor> result = resolver.resolve("com.example.NonExistent", scannerContext);

        assertThat(result).isNotNull();
        assertThat(result.getTypeDescriptor()).isNull();
    }
}
