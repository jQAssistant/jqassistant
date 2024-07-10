package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValidDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/*
tag::doc[]
[[yaml2file]]
== File

A file with the the extension `.yaml` or `.yml`, which can contain
multiple xref:yaml2document[YAML documents].

.Used Combination of Labels
[cols="1h,2"]
|===

tag::labeloverview[]

ifdef::iov[| <<yaml2file,File>>]
ifndef::iov[| Used labels]
| `:Yaml:File`

end::labeloverview[]

|===

end::doc[]
 */
public interface YMLFileDescriptor
    extends YMLDescriptor, FileDescriptor, Descriptor, ValidDescriptor {

/* tag::doc[]

.Relations of a YAML file
[options="header",cols="2,2,1,5"]
|===

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
|===
end::doc[] */

/* tag::doc[]

.Properties of a YAML File
[options="header",cols="2,2,6"]
|===

| Property Name
| Existence
| Description

include::{docRoot}/com/buschmais/jqassistant/plugin/common/api/model/ValidDescriptor.adoc[tags="valid-property"]

|===

In case the scanner of the YAML 2 plugin is not able to scan all documents
of the YAML file, the file will be marked as not valid by setting
the property `valid` of the node representing the YAML file to `false`.
This is done on the file level even if only one document of many is not valid.
This behavior is due to the used
https://bitbucket.org/asomov/snakeyaml-engine[SnakeYAML Engine^] library.


end::doc[] */

}
