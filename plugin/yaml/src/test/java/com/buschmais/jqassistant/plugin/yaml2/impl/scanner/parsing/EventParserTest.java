package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.*;
import org.junit.runners.MethodSorters;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.events.*;

import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("Given a stream of parse events")
@FixMethodOrder(MethodSorters.JVM)
class EventParserTest {
    private EventParser parser = new EventParser();
    private StreamNode rootNode;

    @DisplayName("with one document")
    @Nested
    class WithOneDocument {

        @DisplayName("which is empty")
        @Nested
        class WhichIsEmpty {
            @BeforeEach
            void setUp() {
                Stream<Event> events = Stream.of(strStE(),
                                                 docStE(), docEndE(),
                                                 strEndE());
                rootNode = parser.parse(events);
            }

            @DisplayName("then the parse tree contains one document")
            @Test
            void parseTreeContainsOneDocument() {
                assertThat(rootNode).isNotNull();
                assertThat(rootNode.getDocuments()).hasSize(1);
            }

            @DisplayName("then the document is empty")
            @Test
            void theDocumentIsEmpty() {
                DocumentNode documentNode = rootNode.getDocuments().get(0);

                assertThat(documentNode.getScalars()).isEmpty();
                assertThat(documentNode.getMaps()).isEmpty();
                assertThat(documentNode.getSequences()).isEmpty();
            }
        }

        @DisplayName("with anchors")
        @Nested
        class Anchors {
            @DisplayName("there the one anchor if on the only scalar value")
            @Nested
            class AnchorOnSingleScalarValue {
                @BeforeEach
                void setUp() {
                    Stream<Event> events = Stream.of(strStE(),
                                                     docStE(),
                                                     seqStE(),
                                                     scalarE("L1", anchor("anchor1")),
                                                     seqEndE(),
                                                     docEndE(),
                                                     strEndE());

                    rootNode = parser.parse(events);
                }

                @DisplayName("then the parse tree contains the this anchor on the right node")
                @Test
                void anchorAForAScalarValueRecognized() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode topSeqNode = documentNode.getSequences().get(0);

                    assertThat(topSeqNode.getScalars()).hasSize(1);
                    ScalarNode scalarNode = topSeqNode.getScalars().get(0);

                    assertThat(scalarNode.getAnchor()).isPresent();
                    assertThat(scalarNode.getAnchor()).get().isEqualTo("anchor1");
                }
            }

            @DisplayName("there is on anchor on one of multiple scalar values")
            @Nested
            class AnchorOnOfManyScalarValues {

                @BeforeEach
                void setUp() {
                    Stream<Event> events = Stream.of(strStE(),
                                                     docStE(),
                                                     seqStE(),
                                                     scalarE("L0"),
                                                     scalarE("L1", anchor("anchor2")),
                                                     scalarE("L2"),
                                                     seqEndE(),
                                                     docEndE(),
                                                     strEndE());

                    rootNode = parser.parse(events);
                }

                @DisplayName("then the scalar with the anchor can be found in the parser's anchor cache")
                @Test
                void anchorCanBeFoundInTheAliasReference() {
                    assertThat(parser.hasAnchor("anchor2")).isTrue();
                    assertThat(parser.getAnchor("anchor2")).get().isInstanceOf(ScalarNode.class);

                    ScalarEvent scalarEvent = (ScalarEvent) parser.getAnchor("anchor2").get().getEvent();

                    assertThat(scalarEvent.getValue()).isEqualTo("L1");
                }
            }
        }

        @DisplayName("with complex keys")
        @Nested
        class ComplexKey {

            @DisplayName("there the complex key is a sequence")
            @Nested
            class TheComplexKeyIsASequence {

                @BeforeEach
                void setUp() throws IOException {
                    rootNode = getParseTreeFromFile("/spec-examples/c2-e11-mapping-between-sequences.yaml");
                }

                @DisplayName("then the key value of each complex key is a sequence")
                @Test
                void theKeyOfAComplexKeyIsASequence() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    MapNode mapNode = documentNode.getMaps().get(0);

                    assertThat(mapNode.getComplexKeys()).allSatisfy(ck -> {
                        assertThat(ck.getKeyNode()).isNotNull().isInstanceOf(SequenceNode.class);
                        assertThat(ck.getValue()).isNotNull().isInstanceOf(SequenceNode.class);
                    });
                }

                @DisplayName("then the map in the document has two complex key")
                @Test
                void theMapHasTwoComplexKeys() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    MapNode mapNode = documentNode.getMaps().get(0);

                    assertThat(mapNode.getComplexKeys()).hasSize(2);

                }

                @DisplayName("then the document contains one map")
                @Test
                void theDocumentContainsOneMap() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);

                    assertThat(documentNode).isNotNull();
                    assertThat(documentNode.getMaps()).hasSize(1);
                }
            }
        }

        @DisplayName("with a sequence at the toplevel")
        @Nested
        class SequenceLevel {

            @DisplayName("and with a scalar value")
            @Nested
            class WithScalarValue {
                @BeforeEach
                void setUp() {
                    Stream<Event> events = Stream.of(strStE(),
                                                     docStE(),
                                                     seqStE(),
                                                     scalarE("L1"),
                                                     seqEndE(),
                                                     docEndE(),
                                                     strEndE());

                    rootNode = parser.parse(events);
                }

                @DisplayName("then the sequence has one scalar")
                @Test
                void theSequenceHasOneScalar() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode topSeqNode = documentNode.getSequences().get(0);

                    assertThat(topSeqNode.getScalars()).hasSize(1);
                    assertThat(topSeqNode.getSequences()).isEmpty();
                    assertThat(topSeqNode.getMaps()).isEmpty();
                }
            }

            @DisplayName("with a map as value")
            @Nested
            class WithAMapAsValue {
                @BeforeEach
                void setUp() {
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

                    rootNode = parser.parse(events);
                }

                @DisplayName("then the sequence contains a map")
                @Test
                void sequencesWithAMap() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode sequenceNode = documentNode.getSequences().get(0);

                    assertThat(sequenceNode.getMaps()).hasSize(1);
                }

                @DisplayName("then the map contains only one simple key")
                @Test
                void theMapContainsOnlyOneSimpleKey() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode sequenceNode = documentNode.getSequences().get(0);
                    MapNode mapNode = sequenceNode.getMaps().get(0);

                    assertThat(mapNode.getSimpleKeys()).hasSize(1);
                    assertThat(mapNode.getAliasKeys()).isEmpty();
                    assertThat(mapNode.getComplexKeys()).isEmpty();
                }
            }

            @DisplayName("and a sequence as item of the toplevel sequence")
            @Nested
            class AndASequenceAsItemOfTheTopLevelSequence {
                @BeforeEach
                void setUp() {
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

                    rootNode = parser.parse(events);
                }

                @DisplayName("then the parser tree contains exactly one document")
                @Test
                void exactlyOneDocument() {
                    assertThat(rootNode.getDocuments()).hasSize(1);
                }

                @DisplayName("then the top level sequence has two items")
                @Test
                void sequenceHasTwoItems() {

                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode topSeqNode = documentNode.getSequences().get(0);

                    assertThat(topSeqNode.getItemsTotal()).isEqualTo(2);
                    assertThat(topSeqNode.getScalars()).hasSize(1);
                    assertThat(topSeqNode.getSequences()).hasSize(1);
                }

                @DisplayName("then the subsequence as exactly one item")
                @Test
                void valueOfTheSubsequenceHasOneItem() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode topSeqNode = documentNode.getSequences().get(0);
                    SequenceNode chldSequenceNode = topSeqNode.getSequences().get(0);

                    assertThat(chldSequenceNode.getItemsTotal()).isEqualTo(1);
                }

                @DisplayName("then the value of the subsequence is a scalar")
                @Test
                void valueOfTheSubsequenceIsAScalar() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode topSeqNode = documentNode.getSequences().get(0);
                    SequenceNode chldSequenceNode = topSeqNode.getSequences().get(0);

                    assertThat(chldSequenceNode.getScalars()).hasSize(1);
                }

            }
        }

        @DisplayName("with a map on the top level")
        @Nested
        class MapLevel {

            @DisplayName("and a simple key and a scalar as value")
            @Nested
            class AndASimpleKeyAndAScalarValue {

                @BeforeEach
                void setUp() {
                    Stream<Event> events = Stream.of(strStE(),
                                                     docStE(),
                                                     mapStE(),
                                                     scalarE("K"),
                                                     scalarE("V"),
                                                     mapEndE(),
                                                     docEndE(),
                                                     strEndE());

                    rootNode = parser.parse(events);
                }

                @DisplayName("then the map contains one simple key")
                @Test
                void theMapContainsASimpleKey() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    MapNode mapNode = documentNode.getMaps().get(0);

                    assertThat(mapNode.getSimpleKeys()).hasSize(1);
                }

                @DisplayName("then the key of the map is a scalar value")
                @Test
                void theKeyIsAScalar() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    MapNode mapNode = documentNode.getMaps().get(0);
                    KeyNode keyNode = mapNode.getSimpleKeys().get(0);

                    assertThat(keyNode.getValue()).isInstanceOf(ScalarNode.class);
                }

                @DisplayName("then the value of the key is a scalar with the correct value")
                @Test
                void theValueOfTheKeyIsCorrect() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    MapNode mapNode = documentNode.getMaps().get(0);
                    KeyNode keyNode = mapNode.getSimpleKeys().get(0);
                    ScalarNode valueNode = (ScalarNode) keyNode.getValue();

                    assertThat(valueNode.getScalarValue()).isEqualTo("V");
                }
            }

            @DisplayName("and a sequence as value")
            @Nested
            class AndASequenceAsValue {
                @BeforeEach
                void setUp() {
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

                    rootNode = parser.parse(events);
                }

                @DisplayName("then the map has exactly one key")
                @Test
                void mapHasOneKey() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    MapNode mapNode = documentNode.getMaps().get(0);

                    assertThat(mapNode.getSimpleKeys()).hasSize(1);
                }

                @DisplayName("then the value of the map is a sequence")
                @Test
                void valueOfMapIsASequence() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    MapNode mapNode = documentNode.getMaps().get(0);
                    KeyNode keyNode = mapNode.getSimpleKeys().get(0);

                    assertThat(keyNode.getValue()).isInstanceOf(SequenceNode.class);
                }
            }

            @Nested
            @DisplayName("and maps as values")
            class AndAMapWithTwoKeys {
                @BeforeEach
                void setUp() throws IOException {
                    rootNode = getParseTreeFromFile("/spec-examples/c2-e06-mapping-of-mappings.yaml");
                }

                @DisplayName("then the document contains one map ")
                @Test
                void theParseTreeContainsOneDocument() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);

                    assertThat(documentNode.getMaps()).hasSize(1);
                }

                @DisplayName("then the map contains two simple keys")
                @Test
                void withTwoMap3s() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    MapNode mapNode = documentNode.getMaps().get(0);

                    assertThat(mapNode.getSimpleKeys()).hasSize(2);
                }
            }
        }

        @DisplayName("with anchors and aliases")
        @Nested
        class AliasAndAnchor {
            @DisplayName("there one item in the toplevel sequence aliases another item")
            @Nested
            class SequenceItemAliasesAnotherOne {
                @BeforeEach
                void setUp() {
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

                    rootNode = parser.parse(events);
                }

                @DisplayName("then it alias item is the third item in the sequence")
                @Test
                void theAlisItemIsTheThirdItem() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode sequenceNode = documentNode.getSequences().get(0);

                    assertThat(sequenceNode.getAliases()).isNotEmpty().hasSize(1);
                    AliasNode aliasNode = sequenceNode.getAliases().get(0);

                    assertThat(aliasNode.getAliasedNode()).isInstanceOf(ScalarNode.class);
                    assertThat(aliasNode.getIndex()).get().isEqualTo(3);
                }

                @DisplayName("then the alias references the correct item")
                @Test
                void theAliasReferencesTheCorrectItem() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);
                    SequenceNode sequenceNode = documentNode.getSequences().get(0);
                    AliasNode aliasNode = sequenceNode.getAliases().get(0);

                    assertThat(aliasNode.getAliasedNode()).isInstanceOf(ScalarNode.class);
                    ScalarNode scalarNode = (ScalarNode) aliasNode.getAliasedNode();
                    assertThat(scalarNode.getScalarValue()).isEqualTo("L1");
                }
            }

            @DisplayName("there the anchor is on the key of a complex key and the alias is used as key in the same map")
            @Nested
            class AnchorOnKeyOfMapInComplexKeyAndAliasAsKeyInMap {

                @BeforeEach
                void setUp() throws IOException {
                    rootNode = getParseTreeFromFile("/anchor/toplevel-map-anchor-in-complexkey-on-key-alias-is-also-key.yml");
                }


                @DisplayName("then the parse tree contains a single document")
                @Test
                void name1() {
                    assertSoftly(sa -> {
                        sa.assertThat(rootNode).isNotNull();
                        sa.assertThat(rootNode.getDocuments()).hasSize(1);
                    });
                }

                @DisplayName("then the found document has only one single map")
                @Test
                void foundDocumentHasOnlyOneSingleMap() {
                    DocumentNode documentNode = rootNode.getDocuments().get(0);

                    assertThat(documentNode.getMaps()).hasSize(1);
                    assertThat(documentNode.getSequences()).isEmpty();
                    assertThat(documentNode.getScalars()).isEmpty();
                }


                @DisplayName("then the map of the document has a complex key and an alias key")
                @Test
                void thenTheMapOfTheDocumentHasAComplexKeyAndAliasKey() {
                    MapNode topLevelMapNode = rootNode.getDocuments().get(0).getMaps().get(0);

                    assertThat(topLevelMapNode.getComplexKeys()).hasSize(1);
                    assertThat(topLevelMapNode.getSimpleKeys()).isEmpty();
                    assertThat(topLevelMapNode.getAliasKeys()).hasSize(1);
                }

                @DisplayName("then the alias key has the correct key name and the correct value")
                @Test
                void thenTheAliasKeyHasTheCorrectValue() {
                    AliasKeyNode aliasKeyNode = rootNode.getDocuments().get(0).getMaps().get(0).getAliasKeys().get(0);

                    assertThat(aliasKeyNode.getKey()).isInstanceOf(ScalarNode.class);
                    assertThat(((ScalarNode) aliasKeyNode.getKey()).getScalarValue()).isEqualTo("ck1");
                }
            }

            @DisplayName("there the anchor is in a complex key on the key value")
            @Nested
            class AnchorIsInAComplexKeyOnTheKeyValue {
                @BeforeEach
                void setUp() throws IOException {
                    rootNode = getParseTreeFromFile("/anchor/toplevel-map-anchor-in-complexkey-on-key.yml");
                }

                @DisplayName("then the parse tree contains a single document")
                @Test
                void aaa() {
                    assertSoftly(sa -> {
                        sa.assertThat(rootNode).isNotNull();
                        sa.assertThat(rootNode.getDocuments()).hasSize(1);
                    });
                }

                @DisplayName("then the document contains only one map")
                @Test
                void afksfdkasjdhfsfd() {
                    assertSoftly(sa -> {
                        DocumentNode documentNode = rootNode.getDocuments().get(0);
                        sa.assertThat(documentNode.getMaps()).hasSize(1);
                        sa.assertThat(documentNode.getSequences()).isEmpty();
                        sa.assertThat(documentNode.getScalars()).isEmpty();
                    });
                }

                @DisplayName("then the map has one complex key")
                @Test
                void sadfasfsdlkfasdfasd() {
                    MapNode mapNode = rootNode.getDocuments().get(0).getMaps().get(0);

                    assertThat(mapNode.getComplexKeys()).hasSize(1);
                }

                @DisplayName("then the key of the key in the complex key is an anchor")
                @Test
                void a983oi3uoi33io3o() {
                    ComplexKeyNode keyNode = rootNode.getDocuments().get(0)
                                                     .getMaps().get(0).getComplexKeys().get(0);

                    MapNode keyNodeOfMap = (MapNode) keyNode.getKeyNode();

                    assertSoftly(sa -> {
                        sa.assertThat(keyNodeOfMap.getSimpleKeys()).hasSize(1);
                        SimpleKeyNode simpleKeyNode = keyNodeOfMap.getSimpleKeys().get(0);
                        sa.assertThat(simpleKeyNode.getKeyName()).isEqualTo("ck1");
                        sa.assertThat(simpleKeyNode.getKey().getAnchor()).isPresent();
                        sa.assertThat(simpleKeyNode.getKey().getAnchor().get()).isEqualTo("kv");
                    });
                }

                @DisplayName("then the value of the simple key in the top map is the same as the name of the anchor in the complex key")
                @Test
                void djdjdjdddd() {
                    SimpleKeyNode simpleKeyNode = rootNode.getDocuments().get(0)
                                                          .getMaps().get(0).getSimpleKeys().get(0);

                    assertSoftly(sa -> {
                        sa.assertThat(simpleKeyNode.getKeyName()).isEqualTo("zzz");
                        sa.assertThat(simpleKeyNode.getValue()).isInstanceOf(AliasNode.class);
                        sa.assertThat(((AliasNode) simpleKeyNode.getValue()).getAnchorName()).isEqualTo("kv");
                    });
                }
            }
        }
    }

    @DisplayName("with more then one document")
    @Nested
    class DocumentLevel {

        @DisplayName("there both documents are empty")
        @Nested
        class BothDocumentsEmpty {
            @BeforeEach
            void setUp() {
                Stream<Event> events = Stream.of(strStE(),
                                                 docStE(), docEndE(),
                                                 docStE(), docEndE(),
                                                 strEndE());

                rootNode = parser.parse(events);
            }

            @DisplayName("then the parse tree containts two documents")
            @Test
            void streamWithTwoEmptyDocuments() {
                assertThat(rootNode).isNotNull();
                assertThat(rootNode.getDocuments()).hasSize(2);
            }
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

    private StreamNode getParseTreeFromFile(String file) throws IOException {
        LoadSettings settings = LoadSettings.builder().build();
        EventParser eventParser = new EventParser();

        try (InputStream input = this.getClass().getResourceAsStream(file)) {
            Parse parser = new Parse(settings);
            Iterable<Event> events = parser.parseInputStream(input);
            return eventParser.parse(StreamSupport.stream(events.spliterator(), false));
        }
    }

}
