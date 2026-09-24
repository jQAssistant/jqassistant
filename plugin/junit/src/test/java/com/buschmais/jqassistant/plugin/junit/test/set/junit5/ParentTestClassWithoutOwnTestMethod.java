package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ParentTestClassWithoutOwnTestMethod {

    @Nested
    public class ChildTestClass {

        @Test
        void bTest() {
        }

        @Nested
        public class GrandChildTestClass {

            @Test
            void cTest() {
            }
        }
    }
}
