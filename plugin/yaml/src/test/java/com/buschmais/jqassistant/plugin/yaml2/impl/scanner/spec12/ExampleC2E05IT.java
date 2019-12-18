package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

class ExampleC2E05IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e05-sequence-of-sequences.yaml";

    @Override
    String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theDocumentContainsASequence() {
        YMLFileDescriptor fileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = fileDescriptor.getDocuments().get(0);

        assertThat(documentDescriptor.getSequences()).hasSize(1);
    }

    @Test
    void theDocumentContainsOnlyASequence() {
        YMLFileDescriptor fileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = fileDescriptor.getDocuments().get(0);

        assertThat(documentDescriptor.getSequences()).hasSize(1);
        assertThat(documentDescriptor.getMaps()).hasSize(0);
        // todo Check for scalars later

    }

    @Test
    void theDocumentContainsSequenceWithThreeItems() {
        YMLFileDescriptor fileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = fileDescriptor.getDocuments().get(0);
        YMLSequenceDescriptor seqDescriptor = documentDescriptor.getSequences().get(0);

        assertThat(seqDescriptor.getSequences()).hasSize(3);
    }

    @Test
    void theFirstSequenceContainsThreeItems() {
        YMLFileDescriptor fileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = fileDescriptor.getDocuments().get(0);
        YMLSequenceDescriptor topSeqDescriptor = documentDescriptor.getSequences().get(0);
        Optional<YMLSequenceDescriptor> thirdSeqDescriptor = topSeqDescriptor.getSequences().stream().filter(sd -> sd.getIndex() == 1).findFirst();

        assertThat(thirdSeqDescriptor).get().extracting(YMLSequenceDescriptor::getScalars, LIST).hasSize(3);
    }

    @Test
    void theThirdSequenceContainsThreeItems() {
        YMLFileDescriptor fileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = fileDescriptor.getDocuments().get(0);
        YMLSequenceDescriptor topSeqDescriptor = documentDescriptor.getSequences().get(0);
        Optional<YMLSequenceDescriptor> secondSeqDescriptor = topSeqDescriptor.getSequences().stream().filter(sd -> sd.getIndex() == 2).findFirst();

        assertThat(secondSeqDescriptor).get().extracting(YMLSequenceDescriptor::getScalars, LIST).hasSize(3);
    }

    @Test
    void theFirstSequenceContainsTheExpectedItemsInTheCorrectOrder() {
        YMLSequenceDescriptor firstSubSeq = getSequenceX(0);

        Optional<YMLScalarDescriptor> first = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 0).findFirst();
        Optional<YMLScalarDescriptor> second = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 1).findFirst();
        Optional<YMLScalarDescriptor> third = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 2).findFirst();

        assertThat(first).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("name");
        assertThat(second).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("hr");
        assertThat(third).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("avg");
    }

    private YMLSequenceDescriptor getSequenceX(int index) {
        YMLFileDescriptor fileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = fileDescriptor.getDocuments().get(0);
        YMLSequenceDescriptor topSeqDescriptor = documentDescriptor.getSequences().get(0);
        Optional<YMLSequenceDescriptor> firstSeqDescriptor = topSeqDescriptor.getSequences()
                                                                             .stream()
                                                                             .filter(sd -> sd.getIndex() == index)
                                                                             .findFirst();

        return firstSeqDescriptor.orElseThrow(NoSuchElementException::new);
    }

    @Test
    void theSecondSequenceContainsTheExpectedItemsInTheCorrectOrder() {
        YMLSequenceDescriptor firstSubSeq = getSequenceX(1);

        Optional<YMLScalarDescriptor> first = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 0).findFirst();
        Optional<YMLScalarDescriptor> second = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 1).findFirst();
        Optional<YMLScalarDescriptor> third = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 2).findFirst();

        assertThat(first).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("Mark McGwire");
        assertThat(second).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("65");
        assertThat(third).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("0.278");
    }

    @Test
    void theThirdSequenceContainsTheExpectedItemsInTheCorrectOrder() {
        YMLSequenceDescriptor firstSubSeq = getSequenceX(2);

        Optional<YMLScalarDescriptor> first = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 0).findFirst();
        Optional<YMLScalarDescriptor> second = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 1).findFirst();
        Optional<YMLScalarDescriptor> third = firstSubSeq.getScalars().stream().filter(sd -> sd.getIndex() == 2).findFirst();

        assertThat(first).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("Sammy Sosa");
        assertThat(second).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("63");
        assertThat(third).get().extracting(YMLScalarDescriptor::getValue).isEqualTo("0.288");
    }


}
