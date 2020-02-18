package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

// todo Die Datei ist immer valide, aber nicht das Dokument ValidDescriptor

/*
tag::doc[]

==  A YAML File

A file with the the extension `.yaml` or `.yml`, which can contain
multiple xref:yaml2document[YAML documents].

.Used Combination of Labels
[cols="1h,2"]
|====

tag::labeloverview[]

ifdef::iov[| YAML File (`.yaml` or `.yml` extension)]
ifndef::iov[| Used labels]
| `:Yaml:File`

end::labeloverview[]

|====

end::doc[]
 */
public interface YMLFileDescriptor
extends YMLDescriptor, FileDescriptor, Descriptor {

/* tag::doc[]

.Relations of a YAML file
[options="header",cols="2,2,1,5"]
|====

| Relation Name
| Target Node
| Cardinality
| Description

 end::doc[] */


/* tag::doc[]
| `HAS_DOCUMENT`
| xref:yaml2document[]
| 0..n
| References all YAML documents contained in this file
 end::doc[] */
    @Relation("HAS_DOCUMENT")
    List<YMLDocumentDescriptor> getDocuments();

/* tag::doc[]
|====
end::doc[] */

}
