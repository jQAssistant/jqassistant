package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.ArrayList;
import java.util.List;

import org.snakeyaml.engine.v2.events.SequenceStartEvent;

public class SequenceNode extends ParseNode<SequenceStartEvent> {
    private ArrayList<ScalarNode> scalars = new ArrayList<>(5);
    private ArrayList<SequenceNode> sequences = new ArrayList<>(3);
    private int index;
    private ArrayList<MapNode> maps = new ArrayList<>(3);

    public SequenceNode(SequenceStartEvent event) {
        super(event);
    }

    public List<ScalarNode> getScalars() {
        return new ArrayList<>(scalars);
    }

    public void addScalar(ScalarNode node) {
        scalars.add(node);
    }

    public void addSequence(SequenceNode node) {
        sequences.add(node);
    }

    public List<SequenceNode> getSequences() {
        return new ArrayList<>(sequences);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<MapNode> getMaps() {
        return maps;
    }

    public void addMap(MapNode node) {
        maps.add(node);
    }
}
