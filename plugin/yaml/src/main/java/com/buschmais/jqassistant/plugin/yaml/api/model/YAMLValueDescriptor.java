package com.buschmais.jqassistant.plugin.yaml.api.model;

import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("YAMLValue")
public interface YAMLValueDescriptor extends YAMLDescriptor, YAMLValueBucket {
    @Indexed
    @Property("value")
    String getValue();

    void setValue(String value);


    // Required for sequence of sequences...
    @Relation("CONTAINS_VALUE")
    List<YAMLValueDescriptor> getValues();


    /**
     * <p>Returns the position of the value relative to its parent
     * in document order.</p>
     *
     * <p>The position starts with zero and is relative to
     * it's parent. The parent could either be the containing
     * document or a key.</p>
     *
     * @return the position of the key relative to it's parent.
     */
    @Property("position")
    int getPosition();
}
