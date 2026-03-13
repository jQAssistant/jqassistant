package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

public interface YMLIndexable {

/* tag::index-property[]
| `index`
| not always
| Position of the element in document order in a sequence. +
  The `index` property is only a available if this element
is an item in a sequence.
end::index-property[] */

    @Property("index")
    Integer getIndex();

    void setIndex(Integer index);

}
