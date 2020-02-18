package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/* tag::doc[]

[[yaml2map,Map]]
==  A Map

A map as specified in the {yamlSpec}#id2759963[YAML 1.2 specification^].

.A map in a YAML document with three key-value-pairs
[source,yaml]
----
weight: 91
length: 103
height: 44
----

.Used Combination of Labels
[cols="1h,2"]
|====

tag::labeloverview[]

ifdef::iov[| YAML Map]
ifndef::iov[| Used labels]
| `:Yaml:Map`

end::labeloverview[]
|====

end::doc[] */
@Label("Map")
public interface YMLMapDescriptor extends YMLDescriptor, YMLIndexable {

/* tag::doc[]

.Relations of a map
[options="header",cols="2,2,1,5"]
|====

| Relation Name
| Target Node Type
| Cardinality
| Description

end::doc[] */

    /* tag::doc[]
    | `HAS_KEY`
    | ????
    | 1..n
    | Reference to key or a compley key in the map
    end::doc[] */
    // todo rename to getSimpleKeys
    @Relation("HAS_KEY")
    List<YMLSimpleKeyDescriptor> getKeys();

    @Relation("HAS_KEY")
    List<YMLComplexKeyDescriptor> getComplexKeys();

/* tag::doc[]
|====
end::doc[]

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
|====
 end::doc[] */
}
