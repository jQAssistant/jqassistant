package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/* tag::doc[]

[[yaml2simplekey,simple key]]
== Simple Key

A simple key of a key-value pair in a map represented by a scalar
value. Beside the simple key there is also the
<<yaml2complexkey,complex key>>, which can also be used as key
for maps.
Usage examples can be found in {yamlSpec}#id2760395[chapter 2.2
of the YAML specification^].

.A map with a simple key and a complex key in the same map
// Source highlightning does not work currently
// for complex keys with Coderay as highlighter
// Oliver B. Fischer, 2020-02-18
[source]
----
simpleKey: "Value for a simple key"
? - complex
  - key
: "Value for a complex key"
----

.Used Combination of Labels
[cols="1h,2"]
|===

tag::labeloverview[]

ifdef::iov[| <<yaml2item,Simple Key>>]
ifndef::iov[| Used labels]
| `:Yaml:Key:Simple`

end::labeloverview[]

|===

end::doc[] */
@Label("Simple")
public interface YMLSimpleKeyDescriptor extends YMLKeyDescriptor {

/* tag::doc[]

.Relations of a simple key
[options="header",cols="2,2,1,5"]
|===

| Relation Name
| Target Node
| Cardinality
| Description

include::YMLKeyDescriptor.java[tag=has-value-relation]

|===
end::doc[] */


/* tag::doc[]
.Properties of simple key
[options="header",cols="2,2,6"]
|===

| Property Name
| Existence
| Description

include::YMLIndexable.java[tag=index-property]

end::doc[] */

/* tag::doc[]
| `name`
| always
| The name of a key as scalar value
end::doc[] */

    @Property("name")
    String getName();

/* tag::doc[]
|===
end::doc[] */

    void setName(String name);

}
