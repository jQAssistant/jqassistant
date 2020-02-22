package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/*

tag::labeldoc[]

| `:Yaml:Last`
| not always
| The last item in a sequence in document order carries also the
  label `:Last`

end::labeldoc[]

*/
@Label("Last")
public interface YMLFirstDescriptor extends YMLDescriptor {
}
