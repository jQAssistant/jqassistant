package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.events.*;

import static com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.ParsingContextType.Ancestor.FIRST;
import static java.lang.String.format;
import static org.snakeyaml.engine.v2.events.Event.ID.Scalar;

public class EventParser {
    ParserContext parserContext = new ParserContext();
    private ReferenceMap references = new ReferenceMap();

    public StreamNode parse(Stream<Event> events) {
        Consumer<Event> consumer = event -> {
            System.out.println("::: " + event);
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



                // todo add default as error handler
            }
        };

        events.forEachOrdered(consumer);
        return parserContext.getRootNode();
    }

    private void handleAlias(AliasEvent event) {
        String aliasName = event.getAnchor()
                                .orElseThrow(() -> new IllegalStateException("Alias event without anchor name"))
                                .getAnchor();
        AliasNode aliasNode = new AliasNode(event);
        BaseNode<?> referencedNode = references.getAnchor(aliasName).get();
        /*
                                                .orElseThrow(() -> {
                                                    String message = format("No anchor <%s> found", aliasName);
                                                    throw new NoSuchElementException(message);
                                                });
         */
        // todo Pay attention to the context of the alias
        // An alias can be everything
        aliasNode.setReferencedNode(referencedNode);

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
            // todo
            throw new IllegalStateException();
        }

    }

    private void handleMapStart(MappingStartEvent event) {
        MapNode mapNode = new MapNode(event);

        ParsingContextType<BaseNode> contextType = parserContext.getCurrent();

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
            /*
            getScannerContext().getStore().addDescriptorType(mapDescriptor, YMLValueDescriptor.class);
            YMLKeyDescriptor ymlKeyDescriptor = (YMLKeyDescriptor) contextType.getDescriptor();
            ymlKeyDescriptor.setValue(mapDescriptor);
             */
        } else {
            // todo
            throw new IllegalStateException();
        }

        parserContext.enter(inMap);
    }


    private void handleSequenceStart(SequenceStartEvent event) {
        // todo can we assert here something useful?

        SequenceNode sequenceNode = new SequenceNode(event);
        ParsingContextType<SequenceNode> inSequence = ParsingContextType.ofInSequence(sequenceNode);

        if (parserContext.isInDocument()) {
            ((DocumentNode)parserContext.getCurrent().getNode()).addSequence(sequenceNode);
        } else if (parserContext.isInSequence()) {
            int index = parserContext.getCurrent().getPositionalContext().inc();
            sequenceNode.setIndex(index);
            SequenceNode parentSeqNode = (SequenceNode) parserContext.getCurrent().getNode();
            parentSeqNode.addSequence(sequenceNode);
            /*
            int index = parserContext.getCurrent().getPositionalContext().inc();
            ymlSequenceDescriptor.setIndex(index);
            YMLSequenceDescriptor descriptor = (YMLSequenceDescriptor) context.getCurrent().getDescriptor();
            descriptor.getSequences().add(ymlSequenceDescriptor);

             */
        } else if (parserContext.isInKey()) {
            KeyNode keyNode = (KeyNode) parserContext.getCurrent().getNode();
            keyNode.setValue(sequenceNode);
            checkAndHandleAnchor(sequenceNode);
            // YMLSimpleKeyDescriptor keyDescriptor = (YMLSimpleKeyDescriptor) context.getCurrent().getDescriptor();
            // keyDescriptor.setValue(ymlSequenceDescriptor);
        } else {
            // todo check if the type of the exeption is correct or if there is a better one
            String fqcn = parserContext.getCurrent().getNode().getClass().getCanonicalName();
            String message = format("Unsupported YAML element represented by " +
                                    "class %s encountered.", fqcn);
            throw new IllegalStateException(message  );
        }

        parserContext.enter(inSequence);
    }

    private void handleScalar(ScalarEvent event) {
        ParsingContextType<BaseNode> contextType = this.parserContext.getCurrent();

        // todo Add support for tags
        // todo Add support for impl

        if (parserContext.isInSequence()) {
            int index = contextType.getPositionalContext().inc();
            SequenceNode sequenceNode = (SequenceNode) contextType.getNode();
            ScalarNode scalarNode = new ScalarNode(event);

            scalarNode.setIndex(index);
            sequenceNode.addScalar(scalarNode);
            checkAndHandleAnchor(scalarNode);
        } else if (parserContext.isInMap() && event.isEvent(Scalar)) {

            MapNode mapNode = (MapNode) contextType.getNode();
            KeyNode keyNode = new KeyNode(event);
            keyNode.setKeyName(event.getValue());

            ParsingContextType<KeyNode> newContextType = ParsingContextType.ofInKey(keyNode);
            parserContext.enter(newContextType);

            mapNode.addKey(keyNode);
        } else if (parserContext.isInKey() && event.isEvent(Scalar)) {
            ScalarNode scalarNode = new ScalarNode(event);
            KeyNode keyNode = (KeyNode) contextType.getNode();
            //keyNode.setKeyName(event.getValue());
            keyNode.setValue(scalarNode);
            // todo   checkAndHandleAnchor(valueDescriptor);
        } else {
            String fqcn = contextType.getClass().getCanonicalName();
            String message = format("Unsupported YAML element represented by " +
                                    "class %s encountered.", fqcn);
            // todo throw new IllegalStateException(message  );
        }

        // todo Handle unsupported descriptor
    }

    private void checkAndHandleAnchor(BaseNode<?> parseNode) {
        NodeEvent nodeEvent = (NodeEvent) parseNode.getEvent();

        nodeEvent.getAnchor().ifPresent(new Consumer<Anchor>() {
            @Override
            public void accept(Anchor anchor) {
                references.addAnchor(anchor.getAnchor(), parseNode);
            }
        });
        /*
        if (NodeEvent.class.isAssignableFrom(event.getClass())) {
            NodeEvent nodeEvent = NodeEvent.class.cast(event);
            if (nodeEvent.getAnchor().isPresent()) {
                YMLAnchorDescriptor ymlAnchorDescriptor = getScannerContext().getStore()
                                                                             .addDescriptorType(descriptor, YMLAnchorDescriptor.class);
                String alias = nodeEvent.getAnchor().get().getAnchor();
                ymlAnchorDescriptor.setAnchorName(alias);
                context.getAliasCache().addAlias(alias, descriptor);
            }
        }
         */
    }

    private void handleDocumentStart(DocumentStartEvent event) {
        if (parserContext.isNotInStream()) {
            // todo ????
            throwIllegalStateException(ParsingContextType.Type.IN_SEQUENCE, parserContext.peek().getType());
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

    private void throwIllegalStateException(ParsingContextType.Type expected,
                                            ParsingContextType.Type actual) {
        // todo Which type of exception to throw in case of a wrong state
        String message = format("Wrong internal state during parsing a YAML " +
                                "document. Expected content: %s, actual " +
                                "context: %s", expected, actual);

        throw new IllegalStateException(message);
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
