package com.buschmais.jqassistant.plugin.testng.test.set.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * A TestNG test classes.
 */
public class TestClass {

    @BeforeClass
    public void beforeClass() {
    }

    @BeforeTest
    public void beforeTest() {
    }

    @BeforeMethod
    public void before() {
    }

    @Test
    public void activeTestMethod() {
    }

    @AfterMethod
    public void after() {
    }

    @AfterTest
    public void afterTest() {
    }

    @AfterClass
    public void afterClass() {
    }

}

