package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.events.*;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

// todo improve test coverage based on coverage
@DisplayName("Event parser can")
@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
class EventParserTest {
    EventParser parser = new EventParser();

    @DisplayName("handle anchors")
    @Nested
    class Anchors {
        @DisplayName("for a scalar")
        @Test
        void anchorAForAScalarValueRecognized() {

            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             seqStE(),
                                             scalarE("L1", anchor("anchor")),
                                             seqEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);

            DocumentNode documentNode = root.getDocuments().get(0);
            SequenceNode topSeqNode = documentNode.getSequences().get(0);

            assertThat(topSeqNode.getScalars()).hasSize(1);
            ScalarNode scalarNode = topSeqNode.getScalars().get(0);

            assertThat(scalarNode.getAnchor()).isPresent();
            assertThat(scalarNode.getAnchor()).get().isEqualTo("anchor");
        }

        @DisplayName("for a scalar and adds them to the anchor reference")
        @Test
        void anchorCanBeFoundInTheAliasReference() {

            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             seqStE(),
                                             scalarE("L0"),
                                             scalarE("L1", anchor("anchor")),
                                             scalarE("L2"),
                                             seqEndE(),
                                             docEndE(),
                                             strEndE());

            parser.parse(events);

            assertThat(parser.hasAnchor("anchor")).isTrue();
            assertThat(parser.getAnchor("anchor")).get().isInstanceOf(ScalarNode.class);

            ScalarNode scalarNode = (ScalarNode) parser.getAnchor("anchor").get();

            assertThat(scalarNode.getScalarValue()).isEqualTo("L1");
        }

        @Disabled
        @DisplayName("for a sequence")
        @Test
        void anchorAndAliasForASequenceValue() {
            throw new RuntimeException("This test is not implemented.");
        }

        @Disabled
        @DisplayName("for a map")
        @Test
        void anchorAndAliasForAMapValue() {
            throw new RuntimeException("This test is not implemented.");
        }
    }

    @DisplayName("complex keys")
    @Nested
    class ComplexKey {
        @DisplayName("with a sequence as key node")
        @Test
        void withSequenceAsKeyNode() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             mapStE(),
                                             seqStE(),
                                             scalarE("Detroit Tigers"),
                                             scalarE("Chicago cubs"),
                                             seqEndE(),
                                             seqStE(),
                                             scalarE("2001-07-23"),
                                             seqEndE(),
                                             seqStE(),
                                             scalarE("New York Yankees"),
                                             scalarE("Atlanta Braves"),
                                             seqEndE(),
                                             seqStE(),
                                             seqStE(),
                                             scalarE("2001-07-02"),
                                             seqEndE(),
                                             mapEndE(),
                                             docEndE(),
                                             seqEndE());

            StreamNode streamNode = parser.parse(events);

            DocumentNode documentNode = streamNode.getDocuments().get(0);

            assertThat(documentNode).isNotNull();
            assertThat(documentNode.getMaps()).hasSize(1);

            MapNode mapNode = documentNode.getMaps().get(0);

            assertThat(mapNode.getComplexKeys()).hasSize(2);

            assertThat(mapNode.getComplexKeys()).allSatisfy(ck -> {
                assertThat(ck.getKeyNode()).isNotNull().isInstanceOf(SequenceNode.class);
                assertThat(ck.getValue()).isNotNull().isInstanceOf(SequenceNode.class);
            });
        }
    }

    @DisplayName("aliases")
    @Nested
    class Aliases {
        @DisplayName("for an existing scalar")
        @Test
        void aliasReferencesAScalar() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             seqStE(),
                                             scalarE("L0"),
                                             scalarE("L1", anchor("anchor")),
                                             scalarE("L2"),
                                             alias("anchor"),
                                             seqEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode streamNode = parser.parse(events);
            DocumentNode documentNode = streamNode.getDocuments().get(0);
            SequenceNode sequenceNode = documentNode.getSequences().get(0);

            assertThat(sequenceNode.getAliases()).isNotEmpty().hasSize(1);
            AliasNode aliasNode = sequenceNode.getAliases().get(0);

            assertThat(aliasNode.getReferencedNode()).isInstanceOf(ScalarNode.class);
            assertThat(aliasNode.getIndex()).get().isEqualTo(3);

            ScalarNode scalarNode = (ScalarNode) aliasNode.getReferencedNode();
            assertThat(scalarNode.getScalarValue()).isEqualTo("L1");
        }

        @Disabled
        @DisplayName("for an existing map")
        @Test
        void aliasReferencesAMap() {
            throw new RuntimeException();
        }

        @Disabled
        @DisplayName("for an existing sequence")
        @Test
        void aliasReferencesASequence() {
            throw new RuntimeException();
        }


    }

    @DisplayName("parse a document")
    @Nested
    class DocumentLevel {
        @Disabled
        @DisplayName("with a scalar")
        @Test
        void documentWithScalar() {
            throw new RuntimeException("This test is not implemented.");
        }

        @DisplayName("with a map")
        @Test
        void documentWithMap() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             mapStE(),
                                             scalarE("A"),
                                             scalarE("B"),
                                             mapEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);

            DocumentNode documentNode = root.getDocuments().get(0);

            assertThat(documentNode.getMaps()).hasSize(1);

            MapNode mapNode = documentNode.getMaps().get(0);

            assertThat(mapNode.getSimpleKeys()).hasSize(1);

            // todo add assertions on the value of the key
        }

        @DisplayName("which is empty")
        @Test
        void streamWithAnEmptyDocument() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(), docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);

            assertThat(root).isNotNull();
            assertThat(root.getDocuments()).hasSize(1);
        }

        @DisplayName("which is followed by a second empty document")
        @Test
        void streamWithTwoEmptyDocuments() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(), docEndE(),
                                             docStE(), docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);

            assertThat(root).isNotNull();
            assertThat(root.getDocuments()).hasSize(2);
        }

        @DisplayName("with a sequence")
        @Test
        void documentWithSequence() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             seqStE(),
                                             scalarE("A"),
                                             scalarE("B"),
                                             seqEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);

            DocumentNode documentNode = root.getDocuments().get(0);

            assertThat(documentNode.getSequences()).hasSize(1);

            SequenceNode sequenceNode = documentNode.getSequences().get(0);

            assertThat(sequenceNode.getScalars()).hasSize(2);
        }

    }

    @DisplayName("parse a sequence")
    @Nested
    class SequenceLevel {
        @DisplayName("with a scalar as value")
        @Test
        void sequenceWithOneScalar() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             seqStE(),
                                             scalarE("L1"),
                                             seqEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);

            DocumentNode documentNode = root.getDocuments().get(0);
            SequenceNode topSeqNode = documentNode.getSequences().get(0);

            assertThat(topSeqNode.getScalars()).hasSize(1);
            assertThat(topSeqNode.getSequences()).isEmpty();
        }

        @DisplayName("with a map as value")
        @Test
        void sequencesWithAMap() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             seqStE(),
                                             scalarE("A"),
                                             mapStE(),
                                             scalarE("K"),
                                             scalarE("V"),
                                             mapEndE(),
                                             seqEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);

            DocumentNode documentNode = root.getDocuments().get(0);
            SequenceNode sequenceNode = documentNode.getSequences().get(0);

            assertThat(sequenceNode.getMaps()).hasSize(1);
        }

        @DisplayName("with a sequence as value")
        @Test
        void sequenceWithASequence() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             seqStE(),
                                             scalarE("L1"),
                                             seqStE(),
                                             scalarE("L1"),
                                             seqEndE(),
                                             seqEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);

            DocumentNode documentNode = root.getDocuments().get(0);
            SequenceNode topSeqNode = documentNode.getSequences().get(0);

            assertThat(topSeqNode.getScalars()).hasSize(1);
            assertThat(topSeqNode.getSequences()).hasSize(1);

            SequenceNode chldSequenceNode = topSeqNode.getSequences().get(0);

            assertThat(chldSequenceNode.getScalars()).hasSize(1);
        }
    }

    @DisplayName("parse a map")
    @Nested
    class MapLevel {
        @DisplayName("with a simple key and a scalar as value")
        @Test
        void withSimpleKeyAndScalarValue() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             mapStE(),
                                             scalarE("K"),
                                             scalarE("V"),
                                             mapEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);
            DocumentNode documentNode = root.getDocuments().get(0);
            MapNode mapNode = documentNode.getMaps().get(0);

            assertThat(mapNode.getSimpleKeys()).hasSize(1);
            KeyNode keyNode = mapNode.getSimpleKeys().get(0);

            assertThat(keyNode.getValue()).isInstanceOf(ScalarNode.class);
            ScalarNode valueNode = (ScalarNode) keyNode.getValue();
            assertThat(valueNode.getScalarValue()).isEqualTo("V");
        }

        @DisplayName("with a simple key and a sequence as value")
        @Test
        void withSimpleKeyAndSequenceValue() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             mapStE(),
                                             scalarE("K"),
                                             seqStE(),
                                             scalarE("1"),
                                             scalarE("2"),
                                             seqEndE(),
                                             mapEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);
            DocumentNode documentNode = root.getDocuments().get(0);
            MapNode mapNode = documentNode.getMaps().get(0);

            assertThat(mapNode.getSimpleKeys()).hasSize(1);
            KeyNode keyNode = mapNode.getSimpleKeys().get(0);

            assertThat(keyNode.getValue()).isInstanceOf(SequenceNode.class);
        }

        @DisplayName("with two keys with maps as value")
        @Test
        void withTwoMaps() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             mapStE(),
                                             scalarE("Mark McGwire"),
                                             mapStE(),
                                             scalarE("hr"),
                                             scalarE("65"),
                                             scalarE("avg"),
                                             scalarE("0.278"),
                                             mapEndE(),
                                             scalarE("Sammy Sosa"),
                                             mapStE(),
                                             scalarE("hr"),
                                             scalarE("63"),
                                             scalarE("avg"),
                                             scalarE("0.288"),
                                             mapEndE(),
                                             mapEndE(),
                                             docEndE(),
                                             strEndE());
            StreamNode root = parser.parse(events);

            DocumentNode documentNode = root.getDocuments().get(0);

            assertThat(documentNode.getMaps()).hasSize(1);

            MapNode mapNode = documentNode.getMaps().get(0);

            assertThat(mapNode.getSimpleKeys()).hasSize(2);
        }

        @DisplayName("with a simple key and map as value")
        @Test
        void withSimpleKeyAndMapValue() {
            Stream<Event> events = Stream.of(strStE(),
                                             docStE(),
                                             mapStE(),
                                             scalarE("K"),
                                             mapStE(),
                                             scalarE("K2"),
                                             scalarE("V2"),
                                             mapEndE(),
                                             mapEndE(),
                                             docEndE(),
                                             strEndE());

            StreamNode root = parser.parse(events);
            DocumentNode documentNode = root.getDocuments().get(0);
            MapNode mapNode = documentNode.getMaps().get(0);

            assertThat(mapNode.getSimpleKeys()).hasSize(1);
            KeyNode keyNode = mapNode.getSimpleKeys().get(0);

            assertThat(keyNode.getValue()).isInstanceOf(MapNode.class);

            MapNode subMapNode = (MapNode) keyNode.getValue();

            assertThat(subMapNode.getSimpleKeys()).hasSize(1);
        }
    }



    private AliasEvent alias(String anchorName) {
        Anchor anchor = new Anchor(anchorName);
        return new AliasEvent(Optional.of(anchor));
    }

    private ScalarEvent scalarE(String value) {
        return scalarE(value, null);
    }

    private ScalarEvent scalarE(String value, Anchor anchor) {
        return new ScalarEvent(Optional.ofNullable(anchor),
                               empty(), new ImplicitTuple(true, true),
                               value, ScalarStyle.PLAIN);
    }

    private SequenceEndEvent seqEndE() {
        return new SequenceEndEvent();
    }

    private SequenceStartEvent seqStE() {
        return new SequenceStartEvent(empty(), empty(), false, FlowStyle.BLOCK);
    }

    private DocumentEndEvent docEndE() {
        return new DocumentEndEvent(false, empty(), empty());
    }

    private StreamEndEvent strEndE() {
        return new StreamEndEvent(empty(), empty());
    }

    private MappingEndEvent mapEndE() {
        return new MappingEndEvent();
    }

    private MappingStartEvent mapStE() {
        return new MappingStartEvent(empty(), empty(), true, FlowStyle.BLOCK);
    }

    private DocumentStartEvent docStE() {
        return new DocumentStartEvent(false, empty(), emptyMap(), empty(), empty());
    }

    private StreamStartEvent strStE() {
        return new StreamStartEvent(empty(), empty());
    }

    private Anchor anchor(String anchorName) {
        return new Anchor(anchorName);
    }

}
