package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.ArrayList;
import java.util.List;

import org.snakeyaml.engine.v2.events.DocumentStartEvent;

public class DocumentNode extends AbstractBaseNode
    implements EventSupport<DocumentStartEvent> {
    private final DocumentStartEvent event;
    private final ArrayList<SequenceNode> sequences = new ArrayList<>();
    private final ArrayList<MapNode> maps = new ArrayList<>();
    private final ArrayList<ScalarNode> scalars = new ArrayList<>();

    public DocumentNode(DocumentStartEvent event, int o) {
        super(o);
        this.event = event;
    }

    public List<SequenceNode> getSequences() {
        return sequences;
    }

    public void addSequence(SequenceNode node) {
        sequences.add(node);
    }

    public List<MapNode> getMaps() {
        return maps;
    }

    public void addMap(MapNode node) {
        maps.add(node);
    }

    public List<ScalarNode> getScalars() {
        return scalars;
    }

    public void addScalar(ScalarNode node) {
        scalars.add(node);
    }

    @Override
    public DocumentStartEvent getEvent() {
        return event;
    }

    @Override
    protected String generateTextPresentation() {
        return "=DocumentNode [" + getEvent() + "]";
    }

}
