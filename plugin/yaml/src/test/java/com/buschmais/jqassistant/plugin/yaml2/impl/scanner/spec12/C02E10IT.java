package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.*;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

class C02E10IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/spec-examples/c2-e10-node-for-sammy-sosa-appears-twice-in-this-document.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theSequenceItemForSammySosaIsAnAnchor() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLDocumentDescriptor ymlDocumentDescriptor = getDocuments(ymlFileDescriptor).getDocumentByParsePosition(0);
        YMLMapDescriptor ymlMapDescriptor = getMaps(ymlDocumentDescriptor).getMapByParsePosition(0);
        YMLKeyDescriptor keyDescriptor = getKeys(ymlMapDescriptor).getKeyByName("hr");
        YMLSequenceDescriptor ymlSequenceDescriptor = (YMLSequenceDescriptor) keyDescriptor.getValue();
        YMLScalarDescriptor scalarDescriptor = getScalars(ymlSequenceDescriptor).getScalarBySeqIndex(1);

        assertThat(scalarDescriptor).isNotNull().hasValue("Sammy Sosa");

        // Cannot test if it has a anker label via the JAVA API
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

        String cypherQuery = "MATCH (a:Anchor:Yaml {anchorName: 'SS' }) " +
                             "RETURN a";

        List<Object> result = query(cypherQuery).getColumn("a");

        assertThat(result).isNotEmpty();
    }

    @Test
    void cypherTheValueOfEachMappingHasTheLabelValue() {
        readSourceDocument();

        String cypherQuery = "MATCH (s:Sequence:Value:Yaml) " +
                             "RETURN s";

        List<Object> result = query(cypherQuery).getColumn("s");

        assertThat(result).isNotEmpty();

    }
}
