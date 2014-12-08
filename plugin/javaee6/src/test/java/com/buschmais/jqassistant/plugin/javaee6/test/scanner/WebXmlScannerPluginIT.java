package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationArchiveDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebXmlDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

public class WebXmlScannerPluginIT extends AbstractPluginIT {

    @Test
    public void webXml() {
        File webXml = new File(getClassesDirectory(WebXmlScannerPluginIT.class), "WEB-INF/web.xml");
        store.beginTransaction();
        Scanner scanner = getScanner();
        WebApplicationArchiveDescriptor warDescriptor = store.create(WebApplicationArchiveDescriptor.class);
        scanner.getContext().push(WebApplicationArchiveDescriptor.class, warDescriptor);
        WebXmlDescriptor descriptor = scanner.scan(webXml, "/WEB-INF/web.xml", WebApplicationScope.WAR);
        assertThat(descriptor.getVersion(), equalTo("3.0"));
        scanner.getContext().pop(WebApplicationArchiveDescriptor.class);
        store.commitTransaction();
    }

}
