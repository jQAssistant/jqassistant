package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/* tag::doc[]

[[yaml2alias, alias]]
== An Alias

An alias as specified in the {yamlSpec}#id2786196[YAML 1.2 specification^],
used to reuse previously marked content in a document.

.A Map with reused Content
[source, yaml]
----
"James T. Kirk" : &ufp "United Federation of Planets"
"Hikaru Kato Sulu" : *ufp
----

.Used Combination of Labels
[cols="1h,2"]
|===

tag::labeloverview[]

ifdef::iov[| <<yaml2alias,Alias>>]
ifndef::iov[| Used labels]
| `:Yaml:Alias`

end::labeloverview[]

|===

end::doc[] */
@Label("Alias")
public interface YMLAliasDescriptor extends YMLDescriptor {

    /* tag::doc[]

.Relations of an Alias
[options="header",cols="2,2,1,5"]
|===

| Relation Name
| Target Node Type
| Cardinality
| Description

end::doc[] */

/* tag::doc[]
    | `IS_ALIAS_FOR`
    | xref:yaml2anchor[Anchor]
    | 1
    | Reference to the anchor of this alias in the YAML document
end::doc[] */
    @Relation("IS_ALIAS_FOR")
    YMLDescriptor getAnchor();

/* tag::doc[]
|===
end::doc[] */

    void setAnchor(YMLDescriptor anchor);

/* tag::doc[]
.Properties of an Alias
[options="header",cols="2,2,6"]
|===

| Property Name
| Existence
| Description

include::YMLIndexable.java[tag=index-property]

|===
end::doc[] */

}
