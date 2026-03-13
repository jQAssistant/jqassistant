package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/*

tag::labeldoc[]

| `:Yaml:First`
| not always
| The first item in a sequence in document order
  carries also the label `:First`

end::labeldoc[]

*/
@Label("First")
public interface YMLLastDescriptor extends YMLDescriptor {
}
