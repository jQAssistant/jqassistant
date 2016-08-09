package com.buschmais.jqassistant;

import com.buschmais.jqassistant.core.plugin.impl.JQAssistantPropertiesImpl;
import com.buschmais.jqassistant.neo4jserver.test.ui.pageobjects.IndexPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

/**
 * Test the index UI component.
 */
public class IndexViewIT extends com.buschmais.jqassistant.neo4jserver.test.ui.AbstractUITest {

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
    public void testGetVersion() throws IOException {

        final String version = JQAssistantPropertiesImpl.getInstance().getVersion();
        Assert.assertEquals(version, indexPage.getVersion());
    }
}
