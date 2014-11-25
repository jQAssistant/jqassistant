package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import java.io.File;

import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationArchiveDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

public class WebXmlScannerPluginIT extends AbstractPluginIT {

    @Test
    public void webXml() {
        File webXml = new File(getClassesDirectory(WebXmlScannerPluginIT.class), "WEB-INF/web.xml");
        store.beginTransaction();
        Scanner scanner = getScanner();
        WebApplicationArchiveDescriptor warDescriptor = store.create(WebApplicationArchiveDescriptor.class);
        scanner.getContext().push(WebApplicationArchiveDescriptor.class, warDescriptor);
        scanner.scan(webXml, "/WEB-INF/web.xml", WebApplicationScope.WAR);
        scanner.getContext().pop(WebApplicationArchiveDescriptor.class);
        store.commitTransaction();
    }

}
