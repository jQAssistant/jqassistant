package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.*;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

class ExampleC2E10IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e10-node-for-sammy-sosa-appears-twice-in-this-document.yaml";

    @Override
    String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theSequenceItemForSammySosaIsAnAnchor() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLDocumentDescriptor ymlDocumentDescriptor = getDocuments(ymlFileDescriptor).getDocument(0);
        YMLMapDescriptor ymlMapDescriptor = getMaps(ymlDocumentDescriptor).getMap(0);
        YMLKeyDescriptor keyDescriptor = getKeys(ymlMapDescriptor).getKeyByName("hr");
        YMLSequenceDescriptor ymlSequenceDescriptor = (YMLSequenceDescriptor) keyDescriptor.getValue();
        YMLScalarDescriptor scalarDescriptor = getScalars(ymlSequenceDescriptor).getScalar(1);

        assertThat(scalarDescriptor).isNotNull().hasValue("Sammy Sosa");

        // Cannot test if it has a anker label via the JAVA API
    }

    @Disabled("Test not written yet")
    @Test
    void theAliasForSammySosaWorksAsExpected() {
        readSourceDocument();

        throw new RuntimeException("This test is not implemented.");
    }

    @Test
    void cypherAnchorLabelHasBeenAssignedToSammySosa() {
        readSourceDocument();

        String cypherQuery = "MATCH (a:Scalar:Anchor:Yaml) RETURN a";
        List<Object> result = query(cypherQuery).getColumn("a");

        assertThat(result).hasSize(1);
    }

    @Test
    void cypherTheAnchorCanBeFoundViaTheNameOfTheAnchor() {
        readSourceDocument();

        String cypherQuery = "MATCH (a:Anchor:Yaml {" +
                             "anchorName: 'SS' }) " +
                             "RETURN a";

        List<Object> result = query(cypherQuery).getColumn("a");

        assertThat(result).isNotEmpty();
    }
}
