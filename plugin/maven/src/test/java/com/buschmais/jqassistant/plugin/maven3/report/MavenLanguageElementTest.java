package com.buschmais.jqassistant.plugin.maven3.report;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.xo.api.CompositeObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.plugin.maven3.api.report.Maven.MavenLanguageElement.PomXmlFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class MavenLanguageElementTest {

    @Mock
    private ArtifactDescriptor parentDescriptor;

    @Mock
    private MavenPomXmlDescriptor pomXmlDescriptor;

    @Test
    void mavenPomXmlFileSourceLocation() {
        doReturn("/projects/my-project/pom.xml").when(pomXmlDescriptor)
            .getFileName();

        SourceProvider<CompositeObject> sourceProvider = PomXmlFile.getSourceProvider();

        assertThat(PomXmlFile.getLanguage()).isEqualTo("Maven");
        Optional<FileLocation> optionalFileLocation = sourceProvider.getSourceLocation(pomXmlDescriptor);
        assertThat(optionalFileLocation).isPresent();
        assertThat(optionalFileLocation.get()
            .getFileName()).isEqualTo("/projects/my-project/pom.xml");
    }

    @Test
    void mavenPomXmlFileWithGroupIdValue() {
        doReturn("project").when(pomXmlDescriptor)
            .getGroupId();
        doReturn("module").when(pomXmlDescriptor)
            .getArtifactId();

        SourceProvider<CompositeObject> sourceProvider = PomXmlFile.getSourceProvider();

        assertThat(sourceProvider.getName(pomXmlDescriptor)).isEqualTo("project:module");
    }

    @Test
    void mavenPomXmlFileWithParentGroupIdValue() {
        doReturn(parentDescriptor).when(pomXmlDescriptor)
            .getParent();
        doReturn("project").when(parentDescriptor)
            .getGroup();
        doReturn("module").when(pomXmlDescriptor)
            .getArtifactId();

        SourceProvider<CompositeObject> sourceProvider = PomXmlFile.getSourceProvider();

        assertThat(sourceProvider.getName(pomXmlDescriptor)).isEqualTo("project:module");
    }
}
