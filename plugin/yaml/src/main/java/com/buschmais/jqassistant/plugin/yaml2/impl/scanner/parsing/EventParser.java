package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.snakeyaml.engine.v2.events.*;

import static com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.ParsingContextType.Ancestor.FIRST;
import static java.lang.String.format;
import static org.snakeyaml.engine.v2.events.Event.ID.Scalar;

public class EventParser {
    ParserContext parserContext = new ParserContext();
    private ReferenceMap references = new ReferenceMap();

    public StreamNode parse(Stream<Event> events) {
        Consumer<Event> consumer = event -> {
            switch (event.getEventId()) {
                case Alias:
                    boolean referencedNodeIsValueForKey = parserContext.isInKey();
                    handleAlias((AliasEvent) event);

                    if (referencedNodeIsValueForKey) {
                        parserContext.leave();
                    }
                    break;

                case Scalar:
                    boolean scalarIsValueForKey = parserContext.isInKey();
                    handleScalar((ScalarEvent) event);

                    if (scalarIsValueForKey) {
                        parserContext.leave();
                    }
                    break;

                case StreamStart:
                    handleStreamStart((StreamStartEvent) event);
                    break;

                case DocumentStart:
                    handleDocumentStart((DocumentStartEvent) event);
                    break;

                case SequenceStart:
                    boolean seqIsValueForKey = parserContext.isInKey();
                    handleSequenceStart((SequenceStartEvent) event);
                    parserContext.getCurrent().setKeyForValue(seqIsValueForKey);
                    break;

                case MappingStart:
                    boolean mapIsValueForKey = parserContext.isInKey();
                    handleMapStart((MappingStartEvent) event);
                    parserContext.getCurrent().setKeyForValue(mapIsValueForKey);
                    break;

                case MappingEnd:
                case SequenceEnd:
                case DocumentEnd:
                case StreamEnd:
                    boolean wasValueForKey = parserContext.getCurrent().isKeyForValue();
                    parserContext.leave();

                    if (wasValueForKey) {
                        parserContext.leave();
                    }

                    break;

                default:
                    abortProcessing(parserContext.getCurrent(), event);
            }
        };

        events.forEachOrdered(consumer);
        return parserContext.getRootNode();
    }

    private void abortProcessing(ParsingContextType<BaseNode<?>> context, Event event) {
        ParsingContextType.Type type = context.getType();
        Event.ID eventId = event.getEventId();
        String message = format("Unable to process event '%s' while current parsing context is '%s'",
                                type, eventId);

        throw new IllegalStateException(message);
    }

    private void handleAlias(AliasEvent event) {
        String aliasName = event.getAnchor()
                                // todo use the abort method
                                .orElseThrow(() -> new IllegalStateException("Alias event without anchor name"))
                                .getValue();
        AliasNode aliasNode = new AliasNode(event);
        references.getAnchor(aliasName).ifPresent(aliasNode::setReferencedNode);

        if (parserContext.isInSequence()) {
            ParsingContextType<SequenceNode> context = parserContext.getCurrent();
            int index = context.getPositionalContext().inc();
            aliasNode.setIndex(index);

            SequenceNode sequenceNode = context.getNode();
            sequenceNode.addAlias(aliasNode);
        } else if (parserContext.isInKey()) {
            ParsingContextType<KeyNode> context = parserContext.getCurrent();
            KeyNode keyNode = context.getNode();
            keyNode.setValue(aliasNode);
        } else {
            abortProcessing(parserContext.getCurrent(), event);
        }

    }

    private void handleMapStart(MappingStartEvent event) {
        MapNode mapNode = new MapNode(event);

        ParsingContextType<BaseNode<?>> contextType = parserContext.getCurrent();

        ParsingContextType<MapNode> inMap = ParsingContextType.ofInMap(mapNode);
        checkAndHandleAnchor(mapNode);

        if (parserContext.isInDocument()) {
            DocumentNode documentNode = (DocumentNode) contextType.getNode();
            documentNode.addMap(mapNode);
        } else if (parserContext.isInSequence()) {
            int index = contextType.getPositionalContext().inc();
            SequenceNode sequenceNode = (SequenceNode) parserContext.getCurrent().getNode();
            mapNode.setIndex(index);
            sequenceNode.addMap(mapNode);
        } else if (parserContext.isInKey()) {
            KeyNode keyNode = (KeyNode) parserContext.getCurrent().getNode();
            keyNode.setValue(mapNode);
        } else if (parserContext.isInMap()) {
            ComplexKeyNode complexKey = new ComplexKeyNode(event);
            complexKey.setKeyNode(mapNode);
            ParsingContextType<ComplexKeyNode> inComplexKey = ParsingContextType.ofInComplexKey(complexKey);
            MapNode parentMapNode = (MapNode) parserContext.getCurrent().getNode();
            parentMapNode.addKey(complexKey);
            parserContext.enter(inComplexKey);
        } else {
            abortProcessing(parserContext.getCurrent(), event);
        }

        parserContext.enter(inMap);
    }


    private void handleSequenceStart(SequenceStartEvent event) {
        SequenceNode sequenceNode = new SequenceNode(event);
        ParsingContextType<SequenceNode> inSequence = ParsingContextType.ofInSequence(sequenceNode);

        if (parserContext.isInDocument()) {
            ((DocumentNode)parserContext.getCurrent().getNode()).addSequence(sequenceNode);
        } else if (parserContext.isInSequence()) {
            int index = parserContext.getCurrent().getPositionalContext().inc();
            sequenceNode.setIndex(index);
            SequenceNode parentSeqNode = (SequenceNode) parserContext.getCurrent().getNode();
            parentSeqNode.addSequence(sequenceNode);
        } else if (parserContext.isInMap()) {
            ComplexKeyNode complexKey = new ComplexKeyNode(event);
            complexKey.setKeyNode(sequenceNode);
            ParsingContextType<ComplexKeyNode> inComplexKey = ParsingContextType.ofInComplexKey(complexKey);
            MapNode mapNode = (MapNode) parserContext.getCurrent().getNode();
            mapNode.addKey(complexKey);
            parserContext.enter(inComplexKey);
            checkAndHandleAnchor(sequenceNode);
        } else if (parserContext.isInKey()) {
            KeyNode keyNode = (KeyNode) parserContext.getCurrent().getNode();
            keyNode.setValue(sequenceNode);
            checkAndHandleAnchor(sequenceNode);
        } else {
            abortProcessing(parserContext.getCurrent(), event);
        }

        parserContext.enter(inSequence);
    }

    private void handleScalar(ScalarEvent event) {
        ParsingContextType<BaseNode<?>> contextType = this.parserContext.getCurrent();

        if (parserContext.isInSequence()) {
            int index = contextType.getPositionalContext().inc();
            SequenceNode sequenceNode = (SequenceNode) contextType.getNode();
            ScalarNode scalarNode = new ScalarNode(event);

            scalarNode.setIndex(index);
            sequenceNode.addScalar(scalarNode);
            checkAndHandleAnchor(scalarNode);
        } else if (parserContext.isInMap() && event.getEventId() == Scalar) {

            MapNode mapNode = (MapNode) contextType.getNode();
            SimpleKeyNode keyNode = new SimpleKeyNode(event);
            keyNode.setKeyName(event.getValue());

            ParsingContextType<KeyNode> newContextType = ParsingContextType.ofInKey(keyNode);
            parserContext.enter(newContextType);

            mapNode.addKey(keyNode);
        } else if (parserContext.isInKey() && event.getEventId() == Scalar) {
            ScalarNode scalarNode = new ScalarNode(event);
            KeyNode keyNode = (KeyNode) contextType.getNode();
            keyNode.setValue(scalarNode);
            checkAndHandleAnchor(scalarNode);
        } else if (parserContext.isInDocument() && event.getEventId() == Scalar) {
            DocumentNode documentNode = (DocumentNode) contextType.getNode();
            ScalarNode scalarNode = new ScalarNode(event);
            documentNode.addScalar(scalarNode);
            checkAndHandleAnchor(scalarNode);
        } else {
            abortProcessing(parserContext.getCurrent(), event);
        }
    }

    private void checkAndHandleAnchor(BaseNode<?> parseNode) {
        NodeEvent nodeEvent = (NodeEvent) parseNode.getEvent();

        nodeEvent.getAnchor()
                 .ifPresent(anchor -> references.addAnchor(anchor.getValue(), parseNode));
    }

    private void handleDocumentStart(DocumentStartEvent event) {
        if (parserContext.isNotInStream()) {
            abortProcessing(parserContext.getCurrent(), event);
        }

        DocumentNode documentNode = new DocumentNode(event);
        ParsingContextType<DocumentNode> inDocument = ParsingContextType.ofInDocument(documentNode);

        parserContext.enter(inDocument);

        ParsingContextType<StreamNode> streamContext = parserContext.getAncestor(FIRST);
        StreamNode streamNode = streamContext.getNode();
        streamNode.addDocument(documentNode);
    }


    private void handleStreamStart(StreamStartEvent event) {
        StreamNode node = new StreamNode(event);
        ParsingContextType<?> inStream = ParsingContextType.ofInStream(node);
        parserContext.setRootNode(node);
        parserContext.enter(inStream);
    }

    public boolean hasAnchor(String anchor) {
        return references.hasAnchor(anchor);
    }

    public Optional<BaseNode<?>> getAnchor(String anchor) {
        return references.getAnchor(anchor);
    }

    public ReferenceMap getReferenceMap() {
        return references;
    }
}
