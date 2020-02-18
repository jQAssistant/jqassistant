package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/* tag::doc[]

[[yaml2sequence,Sequence]]
==  A Sequence -- :Yaml:Sequence

A sequence as specified in the {yamlSpec}#id2759963[YAML 1.2 specification^].

.A Sequence with two maps as items
[source,yaml]
----
- Humans: Earth
- Hirogen: Delta quadrant
----

end::doc[] */

@Label("Sequence")
public interface YMLSequenceDescriptor extends YMLDescriptor, YMLIndexable {

/* tag::doc[]

.Relations of :Yaml:Sequence
[options="header",cols="2,2,1,5"]
|====

| Relation Name
| Target Node Type
| Cardinality
| Description

end::doc[] */

/* tag::doc[]
| `HAS_ITEM`
|  xref:yaml2scalar[], xref:yaml2sequence[] or xref:yaml2map[]
| 1..n
| Reference to an item in the sequence. The item can be either a
  scalar, a sequence or a map.
end::doc[] */

    @Relation("HAS_ITEM")
    List<YMLSequenceDescriptor> getSequences();

    @Relation("HAS_ITEM")
    List<YMLMapDescriptor> getMaps();

    @Relation("HAS_ITEM")
    List<YMLScalarDescriptor> getScalars();

/* tag::doc[]
|====

end::doc[]
*/

/* tag::doc[]
.Properties of :Yaml:Sequence
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
