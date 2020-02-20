package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/* tag::doc[]

[[yaml2value]]
== A value in a map

A value of a key-value pair in a map can be any valid YAML structure.
The YAML 2 Plugin assigns each structure its standard labels, e.g.
`:Yaml:Map` for a map used as value or `:Yaml:Scalar` to a simple
scalar value like `Romulan Star Empire`. Any YAML structure
used as value of a key-value pair is also labled with `:Value`.

.Used Combination of Labels
[cols="1h,2"]
|===

tag::labeloverview[]

ifdef::iov[| <<yaml2value,Map value>>]
ifndef::iov[| Used labels]
| `:Yaml:Value`

end::labeloverview[]

|===

end::doc[] */
@Label("Value")
public interface YMLValueDescriptor extends YMLDescriptor {
}
