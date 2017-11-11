package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import org.junit.jupiter.api.Test;

public class ParentTestClass {

    @Test
    void aTest() {
    }

    public class ChildTestClass {

        @Test
        void bTest() {
        }

        public class GrandChildTestClass {

            @Test
            void cTest() {
            }

        }
    }
}
