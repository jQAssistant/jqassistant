package com.buschmais.jqassistant.scm.neo4jserver.ui;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.buschmais.jqassistant.core.plugin.impl.JQAssistantPropertiesImpl;
import com.buschmais.jqassistant.scm.neo4jserver.test.AbstractServerTest;
import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Test the index UI component.
 */
public class IndexHtmlTest extends AbstractServerTest {

    @Test
    public void testGetIndexHtml() throws IOException {
        scanClassPathDirectory(getClassesDirectory(AbstractServerTest.class));

        HtmlUnitDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_24);
        webDriver.setJavascriptEnabled(true);

        webDriver.get("http://localhost:7474/jqa/index.html");

        final String version = JQAssistantPropertiesImpl.getInstance().getVersion();
        (new WebDriverWait(webDriver, 30)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.findElement(By.id("version")).getText().equals(version);
            }
        });

        WebElement indexSpan = webDriver.findElement(By.id("version"));

        Assert.assertEquals(indexSpan.getText(), version);
    }
}
