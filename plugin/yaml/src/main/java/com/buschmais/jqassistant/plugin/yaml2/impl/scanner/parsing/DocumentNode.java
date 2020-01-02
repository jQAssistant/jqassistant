package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.ArrayList;
import java.util.List;

import org.snakeyaml.engine.v2.events.DocumentStartEvent;

public class DocumentNode extends BaseNode<DocumentStartEvent> {
    private ArrayList<SequenceNode> sequences = new ArrayList<>();
    private ArrayList<MapNode> maps = new ArrayList<>();

    public DocumentNode(DocumentStartEvent event) {
        super(event);
    }

    public List<SequenceNode> getSequences() {
        return new ArrayList<>(sequences);
    }

    public void addSequence(SequenceNode node) {
        sequences.add(node);
    }

    public List<MapNode> getMaps() {
        return new ArrayList<>(maps);
    }

    public void addMap(MapNode node) {
        maps.add(node);
    }
}
