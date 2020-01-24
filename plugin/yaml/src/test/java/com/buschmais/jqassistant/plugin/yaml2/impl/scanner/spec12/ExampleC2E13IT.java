package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.function.Consumer;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.getDocuments;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
class ExampleC2E13IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e13-in-literals-newlines-are-preserved.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void documentContainsOnlyAScalar() {
        Consumer<YMLScalarDescriptor> condition = (scalar) -> {
            assertThat(scalar).hasValue("\\//||\\/||\n" +
                                        "// ||  ||__\n");
        };

        YMLFileDescriptor fileDescriptor = readSourceDocument();

        YMLDocumentDescriptor document = getDocuments(fileDescriptor).getDocument(0);

        assertThat(document).hasNoSequences();
        assertThat(document).hasNoMaps();
        assertThat(document).hasScalars();
        assertThat(document.getScalars()).element(0).satisfies(condition);
    }

}
