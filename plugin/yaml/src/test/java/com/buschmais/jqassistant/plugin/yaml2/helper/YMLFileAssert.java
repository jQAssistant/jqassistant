package com.buschmais.jqassistant.plugin.yaml2.helper;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;

import org.assertj.core.api.AbstractObjectAssert;

public class YMLFileAssert extends AbstractObjectAssert<YMLFileAssert, YMLFileDescriptor> {


    public YMLFileAssert(YMLFileDescriptor descriptor) {
        super(descriptor, YMLFileAssert.class);
    }

    public YMLFileAssert hasDocuments(int expectedDocuments) {
        isNotNull();
        hasDocuments();

        String assertjErrorMessage = "\nExpecting file descriptor to have <%s> documents\nbut has <%s> documents\n";

        int actualDocuments = actual.getDocuments().size();

        if (actualDocuments != expectedDocuments) {
            failWithMessage(assertjErrorMessage, actualDocuments, expectedDocuments);
        }

        return this;
    }

    public YMLFileAssert hasDocuments() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting file descriptor to have documents";

        if (actual.getDocuments().isEmpty()) {
            failWithMessage(assertjErrorMessage);
        }

        return this;
    }
}
