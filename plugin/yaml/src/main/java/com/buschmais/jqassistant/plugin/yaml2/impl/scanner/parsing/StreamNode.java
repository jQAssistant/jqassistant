package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.ArrayList;
import java.util.List;

import org.snakeyaml.engine.v2.events.StreamStartEvent;

public class StreamNode extends AbstractBaseNode
    implements EventSupport<StreamStartEvent>
{

    private final StreamStartEvent event;
    private ArrayList<DocumentNode> documents = new ArrayList<>();

    public StreamNode(StreamStartEvent event, int o) {
        super(o);
        this.event = event;
    }

    public void addDocument(DocumentNode node) {
        documents.add(node);
    }

    public List<DocumentNode> getDocuments() {
        return new ArrayList<>(documents);
    }

    @Override
    public StreamStartEvent getEvent() {
        return event;
    }

    @Override
    protected String generateTextPresentation() {
        return "=StreamNode [" + getEvent() + "]";
    }

}
