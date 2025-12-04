package com.buschmais.jqassistant.plugin.maven3.report;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.xo.api.CompositeObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.plugin.maven3.api.report.Maven.MavenLanguageElement.Pom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class MavenLanguageElementTest {

    @Mock
    private ArtifactDescriptor parentDescriptor;

    @Mock
    private MavenPomDescriptor pomDescriptor;

    @Mock
    private MavenPomXmlDescriptor pomXmlFileDescriptor;

    @Test
    void mavenPomXmlFileSourceLocation() {
        doReturn("project").when(pomXmlFileDescriptor)
                .getGroupId();
        doReturn("module").when(pomXmlFileDescriptor)
                .getArtifactId();

        assertThat(Pom.getLanguage()).isEqualTo("Maven");

        doReturn("/projects/my-project/pom.xml").when(pomXmlFileDescriptor)
                .getFileName();
        SourceProvider<CompositeObject> sourceProvider = Pom.getSourceProvider();

        assertThat(sourceProvider.getName(pomXmlFileDescriptor)).isEqualTo("project:module");
        Optional<FileLocation> optionalFileLocation = sourceProvider.getSourceLocation(pomXmlFileDescriptor);
        assertThat(optionalFileLocation).isPresent();
        assertThat(optionalFileLocation.get()
            .getFileName()).isEqualTo("/projects/my-project/pom.xml");
    }

    @Test
    void mavenPomSourceLocation() {
        doReturn("project").when(pomDescriptor)
                .getGroupId();
        doReturn("module").when(pomDescriptor)
                .getArtifactId();

        SourceProvider<CompositeObject> sourceProvider = Pom.getSourceProvider();

        assertThat(sourceProvider.getName(pomDescriptor)).isEqualTo("project:module");
        assertThat(Pom.getLanguage()).isEqualTo("Maven");
        Optional<FileLocation> optionalFileLocation = sourceProvider.getSourceLocation(pomXmlFileDescriptor);
        assertThat(optionalFileLocation).isEmpty();
    }

    @Test
    void mavenPomWithParentGroupIdValue() {
        doReturn(parentDescriptor).when(pomDescriptor)
            .getParent();
        doReturn("project").when(parentDescriptor)
            .getGroup();
        doReturn("module").when(pomDescriptor)
            .getArtifactId();

        SourceProvider<CompositeObject> sourceProvider = Pom.getSourceProvider();

        assertThat(sourceProvider.getName(pomDescriptor)).isEqualTo("project:module");
    }
}
