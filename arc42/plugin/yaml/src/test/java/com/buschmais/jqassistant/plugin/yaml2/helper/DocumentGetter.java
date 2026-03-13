package com.buschmais.jqassistant.plugin.yaml2.helper;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;

public class DocumentGetter {

    private final YMLFileDescriptor ymlFileDescriptor;

    public DocumentGetter(YMLFileDescriptor descriptor) {
        ymlFileDescriptor = descriptor;
    }

    public YMLDocumentDescriptor getDocumentByParsePosition(int index) {
        return ymlFileDescriptor.getDocuments().get(index);
    }
}
