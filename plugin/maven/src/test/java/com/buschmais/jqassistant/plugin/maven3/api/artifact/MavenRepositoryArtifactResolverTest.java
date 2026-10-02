package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import java.io.File;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactFileDescriptor;
import com.buschmais.xo.api.Query;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MavenRepositoryArtifactResolverTest {

    public static final File PROJECT_ROOT = new File("target/project");
    public static final File REPOSITORY_ROOT = new File("target/.m2");

    @Mock
    private FileResolver fileResolver;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    @Mock
    private Query.Result<Query.Result.CompositeRowObject> result;

    @Mock
    private Query.Result.CompositeRowObject compositeRowObject;

    @Captor
    private ArgumentCaptor<String> requiredFileCaptor;

    private MavenRepositoryArtifactResolver artifactResolver;

    @BeforeEach
    void setUp() {
        doReturn(PROJECT_ROOT).when(context)
            .getProjectDirectory();
        doReturn(store).when(context)
            .getStore();
        doReturn(Caffeine.newBuilder()
            .build()).when(store)
            .getCache(anyString());
        doReturn(result).when(store)
            .executeQuery(anyString(), anyMap());
        artifactResolver = new MavenRepositoryArtifactResolver(REPOSITORY_ROOT, fileResolver, context);
    }

    @ParameterizedTest
    @MethodSource("coordinates")
    void resolveToRepositoryFile(Coordinates coordinates, String expectedFileName) {
        doReturn(false).when(result)
            .hasResult();
        doReturn(mock(MavenArtifactFileDescriptor.class)).when(fileResolver)
            .require(anyString(), eq(MavenArtifactFileDescriptor.class), eq(context));

        MavenArtifactDescriptor artifactDescriptor = artifactResolver.resolve(coordinates, context);

        assertThat(artifactDescriptor).isNotNull();
        verify(artifactDescriptor).setGroup(coordinates.getGroup());
        verify(artifactDescriptor).setName(coordinates.getName());
        verify(artifactDescriptor).setClassifier(coordinates.getClassifier());
        verify(artifactDescriptor).setType(coordinates.getType());
        verify(artifactDescriptor).setVersion(coordinates.getVersion());
        verify(artifactDescriptor).setFullQualifiedName(anyString());

        verify(fileResolver).require(requiredFileCaptor.capture(), eq(MavenArtifactFileDescriptor.class), eq(context));
        assertThat(requiredFileCaptor.getValue()).isEqualTo("/../.m2" + expectedFileName);
    }

    @Test
    void resolveExistingArtifact() {
        Coordinates coordinates = TestCoordinates.builder()
            .group("com.acme")
            .name("parent")
            .type("jar")
            .version("1.0.0")
            .build();
        doReturn(true).when(result)
            .hasResult();
        doReturn(compositeRowObject).when(result)
            .getSingleResult();
        MavenArtifactFileDescriptor existingMavenArtifactFileDescriptor = mock(MavenArtifactFileDescriptor.class);
        doReturn(existingMavenArtifactFileDescriptor).when(compositeRowObject)
            .get(anyString(), eq(MavenArtifactFileDescriptor.class));

        MavenArtifactDescriptor artifactDescriptor = artifactResolver.resolve(coordinates, context);

        assertThat(artifactDescriptor).isEqualTo(existingMavenArtifactFileDescriptor);
    }

    private static Stream<Arguments> coordinates() {
        return Stream.of(
            //
            of(TestCoordinates.builder()
                .group("com.acme")
                .name("parent")
                .type("jar")
                .version("1.0.0")
                .build(), "/com/acme/parent/1.0.0/parent-1.0.0.jar"),
            //
            of(TestCoordinates.builder()
                .name("parent")
                .type("jar")
                .build(), "/$/parent/$/parent-$.jar"),
            //
            of(TestCoordinates.builder()
                .group("com.acme")
                .name("parent")
                .classifier("jdk1.5")
                .type("jar")
                .version("1.0.0")
                .build(), "/com/acme/parent/1.0.0/parent-1.0.0-jdk1.5.jar"));
    }

    @Builder
    @Getter
    private static class TestCoordinates implements Coordinates {
        private final String group;
        private final String name;
        private final String classifier;
        private final String type;
        private final String version;

    }
}
