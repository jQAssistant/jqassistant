package com.buschmais.jqassistant.plugin.yaml2.helper;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

import org.assertj.core.api.AbstractObjectAssert;

public class YMLSequenceAssert extends AbstractObjectAssert<YMLSequenceAssert, YMLSequenceDescriptor> {


    public YMLSequenceAssert(YMLSequenceDescriptor descriptor) {
        super(descriptor, YMLSequenceAssert.class);
    }

    public YMLSequenceAssert hasItems() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting sequence to have items\n" +
                                     "but the sequences doesn't have any item";

        int actualCount = actual.getScalars().size() +
                          actual.getMaps().size() +
                          actual.getSequences().size();

        if (actualCount <= 0) {
            failWithMessage(assertjErrorMessage);
        }

        return this;
    }

    public YMLSequenceAssert hasItems(int expectedCount) {
        hasItems();

        String assertjErrorMessage = "\nExpecting sequence to have <%s> items\n" +
                                     "but the sequences has <%s> items";

        int size = actual.getScalars().size();
        int size1 = actual.getMaps().size();
        int size2 = actual.getSequences().size();
        int actualCount = size +
                          size1 +
                          size2;

        if (actualCount != expectedCount) {
            failWithMessage(assertjErrorMessage, expectedCount, actualCount);
        }

        return this;
    }

    /*
    public YMLSequenceAssert hasDocuments(int expectedDocuments) {
        isNotNull();
        hasDocuments();

        String assertjErrorMessage = "\nExpecting file descriptor to have <%s> documents\nbut has <%s> documents\n";

        int actualDocuments = actual.getDocuments().size();

        if (actualDocuments != expectedDocuments) {
            failWithMessage(assertjErrorMessage, actualDocuments, expectedDocuments);
        }

        return this;
    }

    public YMLSequenceAssert hasDocuments() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting file descriptor to have documents";

        if (actual.getDocuments().isEmpty()) {
            failWithMessage(assertjErrorMessage);
        }

        return this;
    }
    */
}
