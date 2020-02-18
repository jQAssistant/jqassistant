package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/* tag::doc[]

==  An Anchor -- :Yaml:Anchor

end::doc[] */
@Label("Anchor")
public interface YMLAnchorDescriptor extends YMLDescriptor {
    @Property("anchorName")
    String getAnchorName();

    void setAnchorName(String name);
}
