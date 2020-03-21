package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.anchor;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.*;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

public class AnchorIT extends AbstractYAMLPluginIT {

    @Nested
    class TopLevelMap {
        @Test
        void thePluginHandlesAnAliasForSequenceProperlyAsValueForKeyProperly() {
            YMLFileDescriptor fileDescriptor = readSourceDocument("/anchor/toplevel-map-anchor-on-sequence.yml");

            YMLDocumentDescriptor document = getDocuments(fileDescriptor).getDocumentByParsePosition(0);
            YMLMapDescriptor map = getMaps(document).getMapByParsePosition(0);
            YMLSimpleKeyDescriptor key = getKeys(map).getKeyByName("k2");

            assertThat(key).isNotNull().hasSequenceAsValue();

            YMLSequenceDescriptor sequenceDescriptor = (YMLSequenceDescriptor) key.getValue();

            assertThat(sequenceDescriptor).hasItems(3);
            YMLScalarDescriptor item0 = getScalars(sequenceDescriptor).getScalarBySeqIndex(0);
            YMLScalarDescriptor item1 = getScalars(sequenceDescriptor).getScalarByParsePosition(1);
            YMLScalarDescriptor item2 = getScalars(sequenceDescriptor).getScalarByParsePosition(2);

            assertThat(item0).hasValue("s1v1").withSequenceIndex(0);
            assertThat(item1).hasValue("s1v2").withSequenceIndex(1);
            assertThat(item2).hasValue("s1v3").withSequenceIndex(2);
        }

        @Test
        void cypherAliasOnASequenceCanBeFoundViaCypher() {
            readSourceDocument("/anchor/toplevel-map-anchor-on-sequence.yml");

            String cypherQuery = "MATCH (a:Anchor:Yaml:Sequence {" +
                                 "anchorName: 'alias' }) " +
                                 "RETURN a";

            List<Object> result = query(cypherQuery).getColumn("a");

            assertThat(result).hasSize(1);
        }

        @Test
        void cypherAliasOnValueOfKeyValuePairIsQueryableViaCypher() {
            readSourceDocument("/anchor/toplevel-map-anchor-on-keyvalue.yml");

            String cypherQuery = "MATCH (a:Anchor:Yaml:Value:Scalar {" +
                                 "anchorName: 'a' }) " +
                                 "RETURN a";

            List<Object> result = query(cypherQuery).getColumn("a");

            assertThat(result).hasSize(1);
        }

        @Test
        void cypherAnchorOnSequenceInMapCanBeFoundViaCypher() {
            readSourceDocument("/anchor/toplevel-map-anchor-on-sequence-value.yml");

            String cypherQuery = "MATCH (a:Anchor:Yaml:Value:Sequence) " +
                                 "RETURN a";

            List<Object> result = query(cypherQuery).getColumn("a");

            assertThat(result).hasSize(1);
        }


        @Test
        void thePluginHandlesAnAliasForAMapProperlyAsValueForKeyProperly() {
            YMLFileDescriptor fileDescriptor = readSourceDocument("/anchor/toplevel-map-anchor-on-map.yml");

            YMLDocumentDescriptor document = getDocuments(fileDescriptor).getDocumentByParsePosition(0);
            YMLMapDescriptor map = getMaps(document).getMapByParsePosition(0);
            YMLSimpleKeyDescriptor key = getKeys(map).getKeyByName("kl2");

            assertThat(key).isNotNull().hasMapAsValue();

            YMLMapDescriptor mapDescriptor = (YMLMapDescriptor) key.getValue();

            assertThat(mapDescriptor).containsSimpleKeyWithName("kl2a");
            assertThat(mapDescriptor).containsSimpleKeyWithName("kl2b");
        }

        @Test
        void cypherAnchorOnAMapCanBeFoundViaCypher() {
            readSourceDocument("/anchor/toplevel-map-anchor-on-map.yml");

            String cypherQuery = "MATCH (a:Anchor:Yaml:Map {" +
                                 "anchorName: 'alias' }) " +
                                 "RETURN a";

            List<Object> result = query(cypherQuery).getColumn("a");

            assertThat(result).hasSize(1);
        }

    }

    @Nested
    class TopLevelSequence {

        @Test
        void thePluginHandlesInSequenceAnAliasForScalarProperly() {
            YMLFileDescriptor fileDescriptor = readSourceDocument("/anchor/toplevel-sequence-anchor-on-scalar.yml");

            YMLDocumentDescriptor document = getDocuments(fileDescriptor).getDocumentByParsePosition(0);
            YMLSequenceDescriptor sequence = getSequences(document).getSequenceByParsePosition(0);

            assertThat(sequence).hasItems();
            assertThat(sequence.getScalars()).hasSize(4);

            YMLScalarDescriptor aliasedScalar = getScalars(sequence).getScalarBySeqIndex(2);
            assertThat(aliasedScalar).hasValue("bbbbb").withSequenceIndex(2);
        }

        @Test
        void aliasWithoutAnchorMarksTheWholeFileAsInvalid() {
            readSourceDocument("/anchor/toplevel-sequence-alias-without-anchor.yml");

            String cypherQuery = "MATCH (f:Yaml:File {valid: false}) RETURN f";

            List<YMLFileDescriptor> result = query(cypherQuery).getColumn("f");

            assertThat(result).hasSize(1);
        }


        @Test
        void cypherAliasOnSequenceCanBeFoundViaCypher() {
            readSourceDocument("/anchor/toplevel-sequence-anchor-on-scalar.yml");

            String cypherQuery = "MATCH (a:Anchor:Yaml:Scalar {" +
                                 "anchorName: 'alias' }) " +
                                 "RETURN a";

            List<Object> result = query(cypherQuery).getColumn("a");

            assertThat(result).hasSize(1);
        }

    }

    @Nested
    class TopLevelScalar {
        @Test
        void cypherAnchorOnASingleScalarInADocumentCanBeFoundViaCypher() {
            readSourceDocument("/anchor/toplevel-scalar-with-anchor.yml");

            String cypherQuery = "MATCH (a:Anchor:Yaml:Scalar {" +
                                 "anchorName: 'a', value: 'foobar'}) " +
                                 "RETURN a";

            List<Object> result = query(cypherQuery).getColumn("a");

            assertThat(result).hasSize(1);
        }

    }







}
