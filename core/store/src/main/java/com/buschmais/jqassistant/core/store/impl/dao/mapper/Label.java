package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class Label implements org.neo4j.graphdb.Label {

    private String name;

    public static Label label(Enum<? extends Enum> label) {
        return new Label(label);
    }

    private Label(Enum<? extends Enum> label) {
        this.name = label.name();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        if (!name.equals(label.name)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
