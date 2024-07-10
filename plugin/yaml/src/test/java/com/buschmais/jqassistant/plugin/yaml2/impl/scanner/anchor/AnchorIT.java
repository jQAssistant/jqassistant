package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.anchor;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.AbstractYAMLPluginIT;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.*;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

class AnchorIT extends AbstractYAMLPluginIT {

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
        void cypherAliasOnASequenceCanBeFoundViaCypherResultsInTwoChildSequences() {
            readSourceDocument("/anchor/toplevel-map-anchor-on-sequence.yml");

            String cypherQuery = "MATCH (m:Map:Yaml)-->(:Key)-[:HAS_VALUE]->(s:Sequence)" +
                                 "RETURN s";

            List<Object> result = query(cypherQuery).getColumn("s");

            assertThat(result).hasSize(2);
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
        void cypherAnchorOnSequenceItem() {
            readSourceDocument("/anchor/toplevel-sequence-anchor-on-sequence.yml");

            String cypherQuery = "MATCH (a:Anchor:Yaml:Item:Sequence { index: 0 }) " +
                                 "RETURN a";

            List<Object> result = query(cypherQuery).getColumn("a");

            assertThat(result).hasSize(1);
        }

        @TestStore(type = TestStore.Type.MEMORY)
        @Test
        void cypherAnchorOnSequenceItemResultsInToSequences() {
            readSourceDocument("/anchor/toplevel-sequence-anchor-on-sequence.yml");

            String cypherQuery = "MATCH (doc:Document:Yaml)--> " +
                                 "      (fl:Yaml:Sequence)-[:HAS_ITEM]-> " +
                                 "      (sl:Yaml:Sequence), " +
                                 "      (alias:Yaml:Sequence:Item)-[rel]->(anchor:Yaml:Sequence:Item) " +
                                 "RETURN sl, rel";

            List<Object> sequences = query(cypherQuery).getColumn("sl");
            List<Object> relations = query(cypherQuery).getColumn("rel");

            assertThat(sequences).hasSize(2);
        }

        @TestStore(type = TestStore.Type.MEMORY)
        @Test
        void cypherAnchorOnSequenceItemResultsInCorrectSettingOfFirstAndLand() {
            readSourceDocument("/anchor/toplevel-sequence-anchor-on-sequence.yml");

            String cypherQuery = "MATCH (alias:Yaml:Sequence:Item)-[rel]->(anchor:Yaml:Sequence:Item) " +
                                 "RETURN type(rel) AS type," +
                                 "       alias.index AS pos_alias, " +
                                 "       anchor.index AS pos_anchor";

            List<Object> types = query(cypherQuery).getColumn("type");
            List<Object> posAlias = query(cypherQuery).getColumn("pos_alias");
            List<Object> posAnchor = query(cypherQuery).getColumn("pos_anchor");


            assertThat(types).hasSize(1);
            assertThat(types).containsExactly("IS_ALIAS_FOR");
            assertThat(posAlias).containsExactly(1);
            assertThat(posAnchor).containsExactly(0);
        }



        @Test
        void cypherAnchorOnSequenceItemResultsInTwoChildSequences() {
            readSourceDocument("/anchor/toplevel-sequence-anchor-on-sequence.yml");

            String cypherQuery = "MATCH (d:Yaml:Document)-->(:Yaml:Sequence)" +
                                 "-->(s:Yaml:Sequence) " +
                                 "RETURN s";

            List<Object> result = query(cypherQuery).getColumn("s");

            assertThat(result).hasSize(2);
        }

        @Test
        void cypherAnchorOnSequenceItemResultsInTwoChildMaps() {
            readSourceDocument("/anchor/toplevel-sequence-anchor-on-map.yml");

            String cypherQuery = "MATCH (s:Sequence:Yaml)-->(m:Yaml:Map:Item) " +
                                 "RETURN m";

            List<Object> result = query(cypherQuery).getColumn("m");

            assertThat(result).hasSize(2);
        }

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
