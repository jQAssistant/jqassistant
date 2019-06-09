package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.impl.scanner.ContextType.IN_DOCUMENT;
import static com.buschmais.jqassistant.plugin.yaml2.impl.scanner.ContextType.IN_STREAM;
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
        pc.enter(IN_STREAM);

        assertThat(pc.isEmpty()).isFalse();
        assertThat(pc.peek()).isEqualTo(IN_STREAM);
    }

    @Test
    void leavingAContextWorks() {
        pc.enter(IN_STREAM);
        pc.enter(IN_DOCUMENT);

        assertThat(pc.isEmpty()).isFalse();
        assertThat(pc.peek()).isEqualTo(IN_DOCUMENT);

        pc.leave();

        assertThat(pc.isEmpty()).isFalse();
        assertThat(pc.peek()).isEqualTo(IN_STREAM);
    }

    @Test
    void cantCallLeaveIfThereIsNoCurrentContext() {
        assertThat(pc.isEmpty()).isTrue();
        assertThatThrownBy(() -> pc.leave()).isInstanceOf(IllegalStateException.class)
                                            .withFailMessage("No context available at the moment")
                                            .hasNoCause();
    }
}
