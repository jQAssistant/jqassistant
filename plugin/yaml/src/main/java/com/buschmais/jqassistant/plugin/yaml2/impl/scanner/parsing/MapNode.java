package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.ArrayList;
import java.util.List;

import org.snakeyaml.engine.v2.events.MappingStartEvent;

public class MapNode extends ParseNode<MappingStartEvent> {
    private int index;
    private ArrayList<KeyNode> keys = new ArrayList<>();

    public MapNode(MappingStartEvent event) {
        super(event);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void addKey(KeyNode node) {
        keys.add(node);
    }

    public List<KeyNode> getKeys() {
        return new ArrayList<>(keys);
    }
}
