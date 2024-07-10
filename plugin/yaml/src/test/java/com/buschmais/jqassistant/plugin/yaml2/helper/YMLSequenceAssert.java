package com.buschmais.jqassistant.plugin.yaml2.helper;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

public class YMLSequenceAssert extends AbstractYMLAssert<YMLSequenceAssert, YMLSequenceDescriptor> {


    public YMLSequenceAssert(YMLSequenceDescriptor descriptor) {
        super(descriptor, YMLSequenceAssert.class);
    }

    @Override
    public YMLSequenceAssert andContinueAssertionOnThis() {
        return this;
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

}
