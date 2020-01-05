package com.buschmais.jqassistant.plugin.yaml2.helper;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;

import org.assertj.core.api.AbstractObjectAssert;

public class YMLDocumentAssert extends AbstractObjectAssert<YMLDocumentAssert, YMLDocumentDescriptor> {

    public YMLDocumentAssert(YMLDocumentDescriptor descriptor) {
        super(descriptor, YMLDocumentAssert.class);
    }

    public YMLDocumentAssert hasMaps() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting document descriptor to have maps";

        if (actual.getMaps().isEmpty()) {
            failWithMessage(assertjErrorMessage);
        }

        return this;
    }

    public YMLDocumentAssert hasMaps(int expectedMaps) {
        isNotNull();
        hasMaps();

        String assertjErrorMessage = "\nExpecting document descriptor to have <%s> maps\nbut has <%s> maps\n";

        int actualMaps = actual.getMaps().size();

        if (actualMaps != expectedMaps) {
            failWithMessage(assertjErrorMessage, actualMaps, expectedMaps);
        }

        return this;
    }

    public YMLDocumentAssert hasNoMaps() {
        isNotNull();

        String assertjErrorMaps = "\nExpecting document descriptor to have no maps\nbut has <%s> maps\n";

        int actualMaps = actual.getMaps().size();

        if (!actual.getMaps().isEmpty()) {
            failWithMessage(assertjErrorMaps, actualMaps);
        }

        return this;
    }

    public YMLDocumentAssert hasNoSequences() {
        isNotNull();

        String assertjErrorMaps = "\nExpecting document descriptor to have no sequences\nbut has <%s> sequences\n";

        int actualSequences = actual.getSequences().size();

        if (!actual.getSequences().isEmpty()) {
            failWithMessage(assertjErrorMaps, actualSequences);
        }

        return this;
    }

    public YMLDocumentAssert hasSequences() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting document descriptor to have sequences";

        if (actual.getSequences().isEmpty()) {
            failWithMessage(assertjErrorMessage);
        }


        return this;
    }

    public YMLDocumentAssert hasSequences(int expectedSequences) {
        isNotNull();
        hasSequences();

        String assertjErrorMessage = "\nExpecting document descriptor to have <%s> maps\nbut has <%s> maps\n";

        int actualSequences = actual.getSequences().size();

        if (actualSequences != expectedSequences) {
            failWithMessage(assertjErrorMessage, actualSequences, expectedSequences);
        }

        return this;
    }


    public YMLDocumentAssert hasScalars() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting document descriptor to have scalars";

        if (actual.getScalars().isEmpty()) {
            failWithMessage(assertjErrorMessage);
        }

        return this;
    }

    public YMLDocumentAssert hasNoScalars() {
        isNotNull();

        String assertjErrorMessage = "\nExpecting document descriptor to have no scalars\nbut has <%s> scalars\n";

        if (!actual.getScalars().isEmpty()) {
            failWithMessage(assertjErrorMessage);
        }

        return this;
    }
}
