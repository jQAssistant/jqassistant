package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TagTestClass {

    @Tag("a")
    @Nested
    public class A {
        @Test
        void activeTest() {
        }
    }

    @Tag("b")
    @Nested
    public class B {
        @Tag("bm")
        @Test
        void activeTest() {
        }
    }

    @Nested
    public class C {

        @Tag("c1")
        @Tag("c2")
        @Tag("c3")
        @Test
        void activeTest() {
        }
    }

    @Nested
    @Tag("x")
    @Tag("y")
    public class XY {
        @Test
        void activeTest() {
        }
    }
}
