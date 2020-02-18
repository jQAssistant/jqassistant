package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/* tag::doc[]

[[yaml2simplekey,key]]
== A Key in a Map

A simple key of a key-value pair in a map.

.Used Combination of Labels
[cols="1h,2"]
|====

tag::labeloverview[]

ifdef::iov[| YAML Map]
ifndef::iov[| Used labels]
| `:Yaml:Key:Simple`

end::labeloverview[]
|====

end::doc[] */
@Label("Simple")
public interface YMLSimpleKeyDescriptor extends YMLKeyDescriptor {

/* tag::doc[]

.Relations of a xxxxxx
[options="header",cols="2,2,1,5"]
|====

| Relation Name
| Target Node
| Cardinality
| Description

include::YMLKeyDescriptor.java[tag=has-value-relation]

|====
end::doc[] */


/* tag::doc[]
.Properties of simple key
[options="header",cols="2,2,6"]
|====

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

    // todo rename to key
    @Property("name")
    String getName();

/* tag::doc[]
|====
end::doc[] */

    void setName(String name);

}
