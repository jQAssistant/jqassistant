package com.buschmais.jqassistant.scm.neo4jserver.ui;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.PageFactory;

import com.buschmais.jqassistant.core.plugin.impl.JQAssistantPropertiesImpl;
import com.buschmais.jqassistant.scm.neo4jserver.ui.pageobjects.IndexPage;

/**
 * Test the index UI component.
 */
public class IndexHtmlTest extends AbstractHtmlTest {

    /** The index page. */
    private IndexPage indexPage;

    @Override
    protected String getWebPage() {
        return "index.html";
    }

    @Before
    public void setup() {

        indexPage = PageFactory.initElements(driver, IndexPage.class);
    }

    @Test
    public void testGetIndexHtml() throws IOException {

        //scanClassPathDirectory(getClassesDirectory(AbstractServerTest.class));

        final String version = JQAssistantPropertiesImpl.getInstance().getVersion();
        Assert.assertEquals(version, indexPage.getVersion());
    }
}
