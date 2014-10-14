package com.buschmais.jqassistant.scm.neo4jserver.ui.pageobjects;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

/**
 * Represents the index page.
 */
public class IndexPage {

    /** The web driver. */
    private final WebDriver driver;

    /** The &lt;span/&gt; with the version as text in it. */
    @FindBy(id = "version")
    private WebElement versionField;

    /**
     * A new instance of the page.
     *
     * @param driver
     *            the driver to use
     */
    public IndexPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Get the text of the version &lt;span/&gt;.
     * 
     * @return the version text
     */
    public String getVersion() {

        // the version text is rendered after an AJAX call
        // we will wait until there is any text (or the timeout occurred)
        new WebDriverWait(driver, 5 /* seconds */).pollingEvery(1, TimeUnit.MILLISECONDS).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return (!webDriver.findElement(By.id("version")).getText().isEmpty());
            }
        });
        return versionField.getText();
    }
}
