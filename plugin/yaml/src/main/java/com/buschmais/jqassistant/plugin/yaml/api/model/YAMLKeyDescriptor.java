package com.buschmais.jqassistant.plugin.yaml.api.model;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;

@Label("Key")
public interface YAMLKeyDescriptor extends YAMLDescriptor, NamedDescriptor,
                                           YAMLKeyBucket, YAMLValueBucket,
                                           FullQualifiedNameDescriptor {

    @Relation("CONTAINS_KEY")
    List<YAMLKeyDescriptor> getKeys();

    @Relation("CONTAINS_VALUE")
    List<YAMLValueDescriptor> getValues();

    /**
     * Returns the position of the key relative to its parent
     * in document order.
     *
     * <p>The position starts with zero and is relative to
     * it's parent. The parent could either be the containing
     * document or an other key.</p>
     *
     * @return the position of the key relative to it's parent.
     */
    @Property("position")
    int getPosition();
}
