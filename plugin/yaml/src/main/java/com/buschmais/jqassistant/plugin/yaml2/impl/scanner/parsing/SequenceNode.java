package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.snakeyaml.engine.v2.events.SequenceStartEvent;

public class SequenceNode
    extends BaseNode<SequenceStartEvent> {
    private ArrayList<AliasNode> aliases = new ArrayList<>(5);
    private ArrayList<ScalarNode> scalars = new ArrayList<>(5);
    private ArrayList<SequenceNode> sequences = new ArrayList<>(3);
    private Integer index;
    private ArrayList<MapNode> maps = new ArrayList<>(3);

    public int getItemsTotal() {
        return getScalars().size() +
               getAliases().size() +
               getSequences().size();
    }

    public SequenceNode(SequenceStartEvent event, int o) {
        super(event, o);
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

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Optional<Integer> getIndex() {
        return Optional.ofNullable(index);
    }

    public ArrayList<MapNode> getMaps() {
        return new ArrayList<>(maps);
    }

    public void addMap(MapNode node) {
        maps.add(node);
    }

    public ArrayList<AliasNode> getAliases() {
        return new ArrayList<>(aliases);
    }

    public void addAlias(AliasNode node) {
        aliases.add(node);
    }

    @Override
    protected String generateTextPresentation() {
        return "=SequenceNode [" + getEvent() + "]";
    }
}
