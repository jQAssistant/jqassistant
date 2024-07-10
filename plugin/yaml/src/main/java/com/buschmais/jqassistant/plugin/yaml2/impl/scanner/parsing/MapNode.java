package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.snakeyaml.engine.v2.events.MappingStartEvent;

public class MapNode extends BaseNode<MappingStartEvent> {
    private Integer index;
    private ArrayList<SimpleKeyNode> simpleKeys = new ArrayList<>();
    private ArrayList<ComplexKeyNode> complexKeys = new ArrayList<>();
    private ArrayList<AliasKeyNode> aliasKeys = new ArrayList<>();

    public MapNode(MappingStartEvent event, int o) {
        super(event, o);
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

    public void addKey(AliasKeyNode node) {
        aliasKeys.add(node);
    }

    public List<SimpleKeyNode> getSimpleKeys() {
        return new ArrayList<>(simpleKeys);
    }

    public List<ComplexKeyNode> getComplexKeys() {
        return new ArrayList<>(complexKeys);
    }

    public List<AliasKeyNode> getAliasKeys() {
        return new ArrayList<>(aliasKeys);
    }

    @Override
    protected String generateTextPresentation() {
        return "=MapNode [" + getEvent() + "]";
    }

}
