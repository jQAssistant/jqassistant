package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/* tag::doc[]

[[yaml2anchor]]
== Anchor

An anchor as specified in the {yamlSpec}#id2785586[YAML 1.2 specification],
used to mark content in a YAML document for future reuse in the same document.

.A Map with reused Content
[source, yaml]
----
"James T. Kirk" : &ufp "United Federation of Planets"
"Hikaru Kato Sulu" : *ufp
----

.The resulting Map after processing all Anchors and Aliases
[source, yaml]
----
"James T. Kirk" : "United Federation of Planets"
"Hikaru Kato Sulu" : "United Federation of Planets"
----


.Used Combination of Labels
[cols="1h,2"]
|===

tag::labeloverview[]

ifdef::iov[| <<yaml2value,Anchor>>]
ifndef::iov[| Used labels]
| `:Yaml:Anchor`

end::labeloverview[]

|===

end::doc[] */
@Label("Anchor")
public interface YMLAnchorDescriptor extends YMLDescriptor {

    void setAnchorName(String name);

/* tag::doc[]
.Properties of an Anchor
[options="header",cols="2,2,6"]
|===

| Property Name
| Existence
| Description

end::doc[] */

/* tag::doc[]
| `anchorName`
| always
| The name of the anchor
end::doc[] */
    @Property("anchorName")
    String getAnchorName();

/* tag::doc[]
include::YMLIndexable.java[tag=index-property]

|===
end::doc[] */
}
