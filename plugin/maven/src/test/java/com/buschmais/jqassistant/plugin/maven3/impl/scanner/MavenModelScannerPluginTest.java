package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.util.Properties;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven3.api.model.EffectiveDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.EffectiveModel;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MavenModelScannerPluginTest {

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext context;

    @Mock
    private Store store;

    @Mock
    private ArtifactResolver artifactResolver;

    @Captor
    private ArgumentCaptor<Coordinates> coordinatesCaptor;

    private MavenModelScannerPlugin plugin;

    @BeforeEach
    void setUp() {
        doReturn(context).when(scanner).getContext();
        doReturn(store).when(context).getStore();
        doReturn(artifactResolver).when(context).peek(ArtifactResolver.class);
        plugin = new MavenModelScannerPlugin();
        plugin.initialize();
        plugin.configure(context, emptyMap());
    }

    @AfterEach
    void tearDown() {
        plugin.destroy();

    }

    @Test
    void rawModel() {
        Model model = stubModel();
        MavenPomDescriptor mavenPomDescriptor = verifyModel(model);
        verify(store, never()).addDescriptorType(mavenPomDescriptor, EffectiveDescriptor.class, MavenPomDescriptor.class);
    }

    @Test
    void effectiveModel() {
        Model model = new EffectiveModel(stubModel());
        doAnswer(a -> a.getArgument(0)).when(store).addDescriptorType(any(MavenPomDescriptor.class), eq(EffectiveDescriptor.class), eq(MavenPomDescriptor.class));
        MavenPomDescriptor mavenPomDescriptor = verifyModel(model);
        verify(store).addDescriptorType(mavenPomDescriptor, EffectiveDescriptor.class, MavenPomDescriptor.class);
    }

    private Model stubModel() {
        Model model = mock(Model.class);
        doReturn("com.buschmais.jqassistant").when(model).getGroupId();
        doReturn("test").when(model).getArtifactId();
        doReturn("jar").when(model).getPackaging();
        doReturn("1.0.0").when(model).getVersion();
        doReturn(new Properties()).when(model).getProperties();
        return model;
    }

    private MavenPomDescriptor verifyModel(Model model) {
        MavenPomDescriptor mavenPomDescriptor = mock(MavenPomDescriptor.class);
        doReturn(mavenPomDescriptor).when(context).peek(MavenPomDescriptor.class);
        MavenArtifactDescriptor artifactDescriptor = mock(MavenArtifactDescriptor.class);
        doReturn(artifactDescriptor).when(artifactResolver).resolve(coordinatesCaptor.capture(), eq(context));

        plugin.scan(model, "/pom.xml", MavenScope.PROJECT, scanner);

        verify(mavenPomDescriptor).setGroupId("com.buschmais.jqassistant");
        verify(mavenPomDescriptor).setArtifactId("test");
        verify(mavenPomDescriptor).setPackaging("jar");
        verify(mavenPomDescriptor).setVersion("1.0.0");
        return mavenPomDescriptor;
    }
}
