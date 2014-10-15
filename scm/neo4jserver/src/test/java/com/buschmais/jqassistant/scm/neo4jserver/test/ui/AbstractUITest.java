package com.buschmais.jqassistant.scm.neo4jserver.test.ui;

import org.junit.Assume;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.buschmais.jqassistant.scm.neo4jserver.test.AbstractServerTest;

/**
 * Abstract class for all HTML unit tests.
 */
public abstract class AbstractUITest extends AbstractServerTest {

    /** The base url for all HTMl tests. */
    protected static final String BASE_URL = "http://localhost:7474/jqa/";

    /** The web driver. */
    protected WebDriver driver;

    /**
     * Get the web page that the driver should call.
     * @return the page
     */
    protected abstract String getWebPage();

    /**
     * Setup the web driver
     */
    @Before
    public void initializeWebDriver(){

        try {
            driver = new FirefoxDriver();
        }
        catch (Exception e) {
            Assume.assumeNoException("Unable to load FirefoxDriver", e);
        }

        driver.get(BASE_URL + getWebPage());
    }
}
