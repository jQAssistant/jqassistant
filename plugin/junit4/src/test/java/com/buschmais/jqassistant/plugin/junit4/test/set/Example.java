package com.buschmais.jqassistant.plugin.junit4.test.set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class Example {

    @Test
    public void success() {
    }

    @Test
    public void failure() {
        Assert.fail();
    }

    @Test
    public void error() {
        throw new UnsupportedOperationException();
    }

    @Ignore
    @Test
    public void skipped() {
    }
}
