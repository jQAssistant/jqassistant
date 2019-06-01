package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValidDescriptor;

public interface YAML2FileDescriptor
 extends YAML2Descriptor, FileDescriptor, ValidDescriptor
{

    // todo @Relation("CONTAINS_DOCUMENT")
    // todo List<YAML2DocumentDescriptor> getDocuments();
}
