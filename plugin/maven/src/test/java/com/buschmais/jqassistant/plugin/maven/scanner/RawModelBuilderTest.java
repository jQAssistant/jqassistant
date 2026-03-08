package com.buschmais.jqassistant.plugin.maven.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.buschmais.jqassistant.plugin.maven.api.scanner.RawModelBuilder;

import org.apache.maven.api.model.Model;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RawModelBuilderTest {

    private final RawModelBuilder rawModelBuilder = new RawModelBuilder();

    @Test
    public void validModel() throws IOException {
        File pom = getPOMFile("/child/pom.xml");
        Model model = rawModelBuilder.getModel(pom);
        assertThat(model).isNotNull();
        assertThat(model.getGroupId()).isNull();
        assertThat(model.getArtifactId()).isEqualTo("jqassistant.child");
    }

    @Test
    void invalidModel() throws IOException {
        File pom = getPOMFile("/invalid/pom-with-duplicate-tag.xml");
        assertThat(rawModelBuilder.getModel(pom)).isNull();
    }

    private File getPOMFile(String name) {
        URL resource = RawModelBuilderTest.class.getResource(name);
        return new File(resource.getFile());
    }
}
