package com.buschmais.jqassistant.plugin.junit.test.set.report;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class Example extends AbstractExample {

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
