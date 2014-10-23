package com.buschmais.jqassistant.scm.neo4jserver.test.ui;

import static org.junit.Assume.assumeNoException;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.buschmais.jqassistant.scm.neo4jserver.test.AbstractServerTest;

/**
 * Abstract class for all UI tests.
 */
public abstract class AbstractUITest extends AbstractServerTest {

    /** The base url for all HTMl tests. */
    protected static final String BASE_URL = "http://localhost:" + SERVER_PORT + "/jqa/";

    /** The web driver. */
    protected WebDriver driver;

    /**
     * Get the web page that the driver should call.
     * 
     * @return the page
     */
    protected abstract String getWebPage();

    /**
     * Setup the web driver
     */
    @Before
    public void initializeWebDriver() {
        try {
            driver = new FirefoxDriver();
        } catch (Exception e) {
            assumeNoException("Unable to load FirefoxDriver", e);
        }
        driver.get(BASE_URL + getWebPage());
    }

    @After
    public void shutdownWebDriver() {
        if (driver != null) {
            driver.close();
        }
    }
}
