package com.buschmais.jqassistant.plugin.json.impl.parsing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NestingLevelCounterTest {

    @Test
    public void enterIncreasesTheLevel() {
        NestingLevelCounter c = new NestingLevelCounter(1_0000);

        assertThat(c.level()).isEqualTo(0);

        c.enter();

        assertThat(c.level()).isEqualTo(1);
    }

    @Test
    public void leaveDecreasesTheLevel() {
        NestingLevelCounter c = new NestingLevelCounter(1_0000);

        c.enter();

        assertThat(c.level()).isEqualTo(1);

        c.leave();

        assertThat(c.level()).isEqualTo(0);
    }

    @Test
    public void counterThrowsExceptionIfLevelHasBeenReached() {
        NestingLevelCounter c = new NestingLevelCounter(1);

        assertThatThrownBy(() -> c.enter().check()).isInstanceOf(IllegalStateException.class);
    }

}
