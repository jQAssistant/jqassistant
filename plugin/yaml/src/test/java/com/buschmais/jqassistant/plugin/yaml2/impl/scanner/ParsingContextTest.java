package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
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
        // todo pc.enter(IN_STREAM);

        // todo assertThat(pc.isEmpty()).isFalse();
        // todo assertThat(pc.peek()).isEqualTo(IN_STREAM);
    }

    @Test
    void leavingAContextWorks() {
        // todo pc.enter(IN_STREAM);
        // todo pc.enter(IN_DOCUMENT);

        // todo assertThat(pc.isEmpty()).isFalse();
        // todo assertThat(pc.peek()).isEqualTo(IN_DOCUMENT);

        // todo pc.leave();

        // todo assertThat(pc.isEmpty()).isFalse();
        // todo assertThat(pc.peek()).isEqualTo(IN_STREAM);
    }

    @Test
    void cantCallLeaveIfThereIsNoCurrentContext() {
        assertThat(pc.isEmpty()).isTrue();
        assertThatThrownBy(() -> pc.leave()).isInstanceOf(IllegalStateException.class)
                                            .withFailMessage("No context available at the moment")
                                            .hasNoCause();
    }
}
