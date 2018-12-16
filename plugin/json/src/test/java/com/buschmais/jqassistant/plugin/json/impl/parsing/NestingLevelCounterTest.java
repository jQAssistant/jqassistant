package com.buschmais.jqassistant.plugin.json.impl.parsing;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class NestingLevelCounterTest {

    @Test
    public void enterIncreasesTheLevel() {
        NestingLevelCounter c = new NestingLevelCounter(1_0000);

        assertThat(c.level(), equalTo(0));

        c.enter();

        assertThat(c.level(), equalTo(1));
    }

    @Test
    public void leaveDecreasesTheLevel() {
        NestingLevelCounter c = new NestingLevelCounter(1_0000);

        c.enter();

        assertThat(c.level(), equalTo(1));

        c.leave();

        assertThat(c.level(), equalTo(0));
    }

    @Test
    public void counterThrowsExceptionIfLevelHasBeenReached() {
        NestingLevelCounter c = new NestingLevelCounter(1);

        Assertions.assertThatThrownBy(() -> c.enter().check())
                  .isInstanceOf(IllegalStateException.class);
    }

}
