package com.buschmais.jqassistant.scm.neo4jserver.ui;

import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.buschmais.jqassistant.scm.neo4jserver.test.AbstractServerTest;
import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Abstract class for all HTML unit tests.
 */
public abstract class AbstractHtmlTest extends AbstractServerTest {

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

        HtmlUnitDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_24);
        webDriver.setJavascriptEnabled(true);
        webDriver.get(BASE_URL + getWebPage());

        driver = webDriver;
    }
}
