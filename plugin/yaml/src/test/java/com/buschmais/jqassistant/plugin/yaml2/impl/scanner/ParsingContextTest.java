package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParsingContextTest {

    ParsingContext pc = new ParsingContext();

    @Test
    void aNewlyCreatedContextIsAlwaysEmpty() {
        assertThat(pc.isEmpty()).isTrue();
    }

    @Test
    void enteringAContextWorks() {
        assertThat(pc.isEmpty()).isTrue();
        assertThatThrownBy(() -> pc.peek()).isInstanceOf(IllegalStateException.class)
                                           .withFailMessage("No context available at the moment")
                                           .hasNoCause();
        pc.enter(ContextType.ofInStream());

        assertThat(pc.isEmpty()).isFalse();
        assertThat(pc.peek()).isEqualTo(ContextType.ofInStream());
    }

    @Test
    void leavingAContextWorks() {
        ContextType<YMLDocumentDescriptor> ctxInDoc = ContextType.ofInDocument(Mockito.mock(YMLDocumentDescriptor.class));
        ContextType<YMLDescriptor> ctxInStream = ContextType.ofInStream();

        pc.enter(ctxInStream);
        pc.enter(ctxInDoc);

        assertThat(pc.isEmpty()).isFalse();
        assertThat(pc.peek()).isEqualTo(ctxInDoc);

        pc.leave();

        assertThat(pc.isEmpty()).isFalse();
        assertThat(pc.peek()).isEqualTo(ctxInStream);
    }

    @Test
    void cantCallLeaveIfThereIsNoCurrentContext() {
        assertThat(pc.isEmpty()).isTrue();
        assertThatThrownBy(() -> pc.leave()).isInstanceOf(IllegalStateException.class)
                                            .withFailMessage("No context available at the moment")
                                            .hasNoCause();
    }
}
