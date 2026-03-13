package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.report.Generic;
import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Property;

import static com.buschmais.jqassistant.plugin.common.api.report.Generic.GenericLanguageElement.File;

@Generic(File)
public interface FileNameDescriptor extends Descriptor {

    @Indexed
    @Property("fileName")
    String getFileName();

    void setFileName(String fileName);

}
