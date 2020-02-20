package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/* tag::doc[]

[[yaml2document, Document]]
==  A YAML Document

A single YAML document as specified in the {yamlSpec}#id2800132[YAML 1.2 specification^].
A document can contain either a xref:yaml2scalar[scalar],
a xref:yaml2sequence[sequence] or a xref:yaml2map[map].

.A YAML document with document prefix `---` and suffix `...`
[source,yaml]
----
---
# Comments are not handled by jQAssistant
NX-01: Jonathan Archer
NCC-1701-A: James Tiberius Kirk
NCC-1701-D: Jean-Luc Picard
...
----

.Used Combination of Labels
[cols="1h,2"]
|===

tag::labeloverview[]

ifdef::iov[| YAML Document]
ifndef::iov[| Used labels]
| `:Yaml:Document`

end::labeloverview[]

|===


end::doc[] */
@Label("Document")
public interface YMLDocumentDescriptor extends YMLDescriptor {

/* tag::doc[]

.Relations of :Yaml:Document
[options="header",cols="2,2,1,5"]
|===

| Relation Name
| Target Node Type
| Cardinality
| Description

end::doc[] */

/* tag::doc[]
    | `HAS_SEQUENCE`
    | xref:yaml2sequence[]
    | 0..1
    | Reference to a sequence in the YAML document
end::doc[] */
    @Relation("HAS_SEQUENCE")
    List<YMLSequenceDescriptor> getSequences();

    /* tag::doc[]
    | `HAS_MAP`
    | xref:yaml2map[]
    | 0..1
    | Reference to a map in the YAML document
    end::doc[] */
    @Relation("HAS_MAP")
    List<YMLMapDescriptor> getMaps();

    /* tag::doc[]
    | `HAS_SCALAR`
    | xref:yaml2scalar[]
    | 0..1
    | Reference to a scalar in the YAML document
    end::doc[] */
    @Relation("HAS_SCALAR")
    List<YMLScalarDescriptor> getScalars();

/* tag::doc[]
|===
end::doc[]
*/

}
