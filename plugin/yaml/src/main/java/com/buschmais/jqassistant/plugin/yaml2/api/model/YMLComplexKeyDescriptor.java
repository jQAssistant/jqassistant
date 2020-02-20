package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/* tag::doc[]

[[yaml2complexKey,complex key]]
== Complex Key

A complex key of a key-value pair in a map. A complex key
starts with an `?` and can be any valid YAML structure.
Besides the complex key there is also the
<<yaml2simpleKey,simple key>>, which is represented by
a single scalar value.

.A map with two complex keys
// Source highlightning does not work currently
// for complex keys with Coderay as highlighter
// Oliver B. Fischer, 2020-02-18
[source]
----
simpleKey: "Value for a simple key"
? - Vulcan
  - ShiKahr District
: Spock
? - Earth
  - North America
: James T. Kirk
----

.Used Combination of Labels
[cols="1h,2"]
|===

tag::labeloverview[]

ifdef::iov[| Complex Key]
ifndef::iov[| Used labels]
| `:Yaml:Key:Complex`

end::labeloverview[]

|===

end::doc[] */
@Label("Complex")
public interface YMLComplexKeyDescriptor extends YMLKeyDescriptor {

/* tag::doc[]

.Relations of a complex key
[options="header",cols="2,2,1,5"]
|===

| Relation Name
| Target Node
| Cardinality
| Description

include::YMLKeyDescriptor.java[tag=has-value-relation]

|===
end::doc[] */


    @Relation("HAS_COMPLEX_KEY")
    YMLDescriptor getKey();

    void setKey(YMLDescriptor key);

}
