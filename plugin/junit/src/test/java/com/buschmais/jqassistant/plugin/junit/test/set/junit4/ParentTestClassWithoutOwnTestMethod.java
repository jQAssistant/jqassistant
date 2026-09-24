package com.buschmais.jqassistant.plugin.junit.test.set.junit4;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class ParentTestClassWithoutOwnTestMethod {

    @RunWith(Enclosed.class)
    public static class ChildTestClass {

        @Test
        public void test() {}

        public static class GrandChildTestClass {
            @Test
            public void test() {}
        }
    }
}
