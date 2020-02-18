package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/* tag::doc[]

[[yaml2scalar,Scalar]]

==  A Scalar -- :Yaml:Scalar

A scalar value as described in the {yamlSpec}#id2760844[YAML 1.2 specification^].

end::doc[] */
@Label("Scalar")
public interface YMLScalarDescriptor extends YMLDescriptor, YMLIndexable {
/* tag::doc[]
.Properties of :Yaml:Map
[options="header",cols="2,2,6"]
|====

| Property Name
| Existence
| Description

include::YMLIndexable.java[tag=index-property]

end::doc[] */

/* tag::doc[]
| `value`
| always
| The scalar value itself
end::doc[] */
    @Property("value")
    String getValue();

    void setValue(String value);

/* tag::doc[]
|====
 end::doc[] */

}
