package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.snakeyaml.engine.v2.events.*;

import static java.lang.String.format;

public class EventParser {
    private int tokenIndexSource = 0;
    private ParserContext parserContext = new ParserContext();
    private AnchorCache anchorCache = new AnchorCache();

    int getNextTokenIndex() {
        return tokenIndexSource++;
    }

    public StreamNode parse(Stream<Event> events) {
        Consumer<Event> consumer = event -> {
            switch (event.getEventId()) {
                case Alias:
                    boolean referencedNodeIsValueForKey = parserContext.isInKey();
                    handleAlias((AliasEvent) event);

                    if (referencedNodeIsValueForKey) {
                        getParserContext().leave();
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

    protected ParserContext getParserContext() {
        return parserContext;
    }

    protected AnchorCache getAnchorCache() {
        return anchorCache;
    }


    private void abortProcessing(ParsingContextType<AbstractBaseNode> context, Event event) {
        ParsingContextType.Type type = context.getType();
        Event.ID eventId = event.getEventId();
        String message = format("Unable to process event '%s' while current parsing context is '%s'",
                                type, eventId);

        throw new IllegalStateException(message);
    }

    private void handleAlias(AliasEvent event) {
        String aliasName = event.getAlias().getValue();
        AliasNode aliasNode = new AliasNode(event, getNextTokenIndex());
        anchorCache.getAnchor(aliasName).ifPresent(aliasNode::setAliasedNode);

        if (parserContext.isInSequence()) {
            ParsingContextType<SequenceNode> context = parserContext.getCurrent();
            int index = context.getPositionalContext().inc();
            aliasNode.setIndex(index);

            SequenceNode sequenceNode = context.getNode();
            sequenceNode.addAlias(aliasNode);
        } else if (parserContext.isInKey()) {
            ParsingContextType<KeyNode<?>> context = parserContext.getCurrent();
            KeyNode<?> keyNode = context.getNode();
            keyNode.setValue(aliasNode);
        } else if (parserContext.isInMap()) {
            BaseNode<? extends NodeEvent> referencedNode =
                anchorCache.getAnchor(aliasName)
                           .map((UnaryOperator<BaseNode<? extends NodeEvent>>) node -> {
                              if (node instanceof SimpleKeyNode) {
                                  return ((SimpleKeyNode) node).getKey();
                              }
                              return node;
                          }).get();

            AliasKeyNode keyNode = new AliasKeyNode(event, getNextTokenIndex(), referencedNode);
            ParsingContextType<MapNode> context = parserContext.getCurrent();
            MapNode mapNode = context.getNode();
            mapNode.addKey(keyNode);
            ParsingContextType<AliasKeyNode> inAliasKey = ParsingContextType.ofInAliasKey(keyNode);
            parserContext.enter(inAliasKey);
        } else {
            abortProcessing(parserContext.getCurrent(), event);
        }
    }

    private void handleMapStart(MappingStartEvent event) {
        MapNode mapNode = new MapNode(event, getNextTokenIndex());

        ParsingContextType<AbstractBaseNode> contextType = parserContext.getCurrent();

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
            KeyNode<?> keyNode = (KeyNode<?>) parserContext.getCurrent().getNode();
            keyNode.setValue(mapNode);
        } else if (parserContext.isInMap()) {
            ComplexKeyNode complexKey = new ComplexKeyNode(event, getNextTokenIndex());
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
        SequenceNode sequenceNode = new SequenceNode(event, getNextTokenIndex());
        ParsingContextType<SequenceNode> inSequence = ParsingContextType.ofInSequence(sequenceNode);
        checkAndHandleAnchor(sequenceNode);

        if (parserContext.isInDocument()) {
            ((DocumentNode)parserContext.getCurrent().getNode()).addSequence(sequenceNode);
        } else if (parserContext.isInSequence()) {
            int index = parserContext.getCurrent().getPositionalContext().inc();
            sequenceNode.setIndex(index);
            SequenceNode parentSeqNode = (SequenceNode) parserContext.getCurrent().getNode();
            parentSeqNode.addSequence(sequenceNode);
        } else if (parserContext.isInMap()) {
            ComplexKeyNode complexKey = new ComplexKeyNode(event, getNextTokenIndex());
            complexKey.setKeyNode(sequenceNode);
            ParsingContextType<ComplexKeyNode> inComplexKey = ParsingContextType.ofInComplexKey(complexKey);
            MapNode mapNode = (MapNode) parserContext.getCurrent().getNode();
            mapNode.addKey(complexKey);
            parserContext.enter(inComplexKey);
        } else if (parserContext.isInKey()) {
            KeyNode keyNode = (KeyNode) parserContext.getCurrent().getNode();
            keyNode.setValue(sequenceNode);
        } else {
            abortProcessing(parserContext.getCurrent(), event);
        }

        parserContext.enter(inSequence);
    }

    private void handleScalar(ScalarEvent event) {
        ParsingContextType<AbstractBaseNode> contextType = this.parserContext.getCurrent();

        if (parserContext.isInSequence()) {
            int index = contextType.getPositionalContext().inc();
            SequenceNode sequenceNode = (SequenceNode) contextType.getNode();
            ScalarNode scalarNode = new ScalarNode(event, getNextTokenIndex());

            scalarNode.setIndex(index);
            sequenceNode.addScalar(scalarNode);
            checkAndHandleAnchor(scalarNode);
        } else if (parserContext.isInMap()) {

            MapNode mapNode = (MapNode) contextType.getNode();
            ScalarNode scalarNode = new ScalarNode(event, getNextTokenIndex());
            SimpleKeyNode keyNode = new SimpleKeyNode(scalarNode, getNextTokenIndex());
            checkAndHandleAnchor(keyNode);

            ParsingContextType<KeyNode> newContextType = ParsingContextType.ofInKey(keyNode);
            parserContext.enter(newContextType);

            mapNode.addKey(keyNode);
        } else if (parserContext.isInKey()) {
            ScalarNode scalarNode = new ScalarNode(event, getNextTokenIndex());
            KeyNode<?> keyNode = (KeyNode<?>) contextType.getNode();
            keyNode.setValue(scalarNode);
            checkAndHandleAnchor(scalarNode);
        } else if (parserContext.isInDocument()) {
            DocumentNode documentNode = (DocumentNode) contextType.getNode();
            ScalarNode scalarNode = new ScalarNode(event, getNextTokenIndex());
            documentNode.addScalar(scalarNode);
            checkAndHandleAnchor(scalarNode);
        } else {
            abortProcessing(parserContext.getCurrent(), event);
        }
    }

    private void checkAndHandleAnchor(BaseNode<?> node) {
        node.getEvent().getAnchor().ifPresent(anchor -> anchorCache.addAnchor(node));
    }

    private void handleDocumentStart(DocumentStartEvent event) {
        if (parserContext.isNotInStream()) {
            abortProcessing(parserContext.getCurrent(), event);
        }

        DocumentNode documentNode = new DocumentNode(event, getNextTokenIndex());
        ParsingContextType<DocumentNode> inDocument = ParsingContextType.ofInDocument(documentNode);

        parserContext.enter(inDocument);

        ParsingContextType<StreamNode> streamContext = parserContext.getParent();
        StreamNode streamNode = streamContext.getNode();
        streamNode.addDocument(documentNode);
    }


    private void handleStreamStart(StreamStartEvent event) {
        StreamNode node = new StreamNode(event, getNextTokenIndex());
        ParsingContextType<?> inStream = ParsingContextType.ofInStream(node);
        parserContext.setRootNode(node);
        parserContext.enter(inStream);
    }

    public boolean hasAnchor(String anchor) {
        return anchorCache.hasAnchor(anchor);
    }

    public Optional<BaseNode<? extends NodeEvent>> getAnchor(String anchor) {
        return anchorCache.getAnchor(anchor);
    }

}
