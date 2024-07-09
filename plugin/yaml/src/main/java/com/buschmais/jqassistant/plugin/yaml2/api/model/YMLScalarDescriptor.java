package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/* tag::doc[]

[[yaml2scalar,Scalar]]
== Scalar

A scalar value as described in the {yamlSpec}#id2760844[YAML 1.2 specification^].

.Used Combination of Labels
[cols="1h,2"]
|===

tag::labeloverview[]

ifdef::iov[| Scalar]
ifndef::iov[| Used labels]
| `:Yaml:Scalar`

end::labeloverview[]
|===

end::doc[] */
@Label("Scalar")
public interface YMLScalarDescriptor extends YMLDescriptor, YMLIndexable {
/* tag::doc[]
.Properties of :Yaml:Map
[options="header",cols="2,2,6"]
|===

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
|===
 end::doc[] */

}
