package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import org.junit.jupiter.api.*;

public class StandardTest {
    @BeforeAll
    static void beforeAll() {
    }

    @BeforeEach
    void beforeEach() {
    }

    @Test
    void activeTest() {
    }

    @Test
    @Disabled("foobar")
    void disabledTest() {
    }

    @AfterEach
    void afterEach() {
    }

    @AfterAll
    static void afterAll() {
    }

}
