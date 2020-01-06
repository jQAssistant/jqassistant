package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.snakeyaml.engine.v2.events.MappingStartEvent;

public class MapNode extends BaseNode<MappingStartEvent>
    implements AnchorSupport<MappingStartEvent> {
    private Integer index;
    private ArrayList<SimpleKeyNode> simpleKeys = new ArrayList<>();
    private ArrayList<ComplexKeyNode> complexKeys = new ArrayList<>();

    public MapNode(MappingStartEvent event) {
        super(event);
    }

    public Optional<Integer> getIndex() {
        return Optional.ofNullable(index);
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void addKey(SimpleKeyNode node) {
        simpleKeys.add(node);
    }

    public void addKey(ComplexKeyNode node) {
        complexKeys.add(node);
    }

    public List<SimpleKeyNode> getSimpleKeys() {
        return new ArrayList<>(simpleKeys);
    }

    public ArrayList<ComplexKeyNode> getComplexKeys() {
        return complexKeys;
    }
}
