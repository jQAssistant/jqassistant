package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Abstract
@Label("Key")
public interface YMLKeyDescriptor extends YMLDescriptor {

/* tag::has-value-relation[]

| `HAS_VALUE`
| <<yaml2map>>, <<yaml2sequence>> or <<yaml2scalar>>
| 0..1
| Reference to the value of this key-value pair

end::has-value-relation[] */
    @Relation("HAS_VALUE")
    YMLDescriptor getValue();

    void setValue(YMLDescriptor descriptor);
}
