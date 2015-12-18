package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.scanner.ArtifactScopedTypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.javaee6.api.model.ApplicationXmlDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationArchiveDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebXmlDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.EnterpriseApplicationScope;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

/**
 * Contains integration tests for JavaEE XML descriptors.
 */
public class XmlDescriptorScannerPluginIT extends AbstractPluginIT {

    /**
     * Verify scanning of web.xml descriptors.
     */
    @Test
    public void webXml() {
        store.beginTransaction();
        Scanner scanner = getScanner();
        WebApplicationArchiveDescriptor warDescriptor = store.create(WebApplicationArchiveDescriptor.class);
        scanner.getContext().push(TypeResolver.class, new ArtifactScopedTypeResolver(warDescriptor));
        scanner.getContext().push(WebApplicationDescriptor.class, warDescriptor);
        File webXml = new File(getClassesDirectory(XmlDescriptorScannerPluginIT.class), "WEB-INF/web.xml");
        WebXmlDescriptor descriptor = scanner.scan(webXml, "/WEB-INF/web.xml", WebApplicationScope.WAR);
        assertThat(descriptor.getVersion(), equalTo("3.0"));
        scanner.getContext().pop(WebApplicationDescriptor.class);
        scanner.getContext().pop(TypeResolver.class);
        store.commitTransaction();
    }

    /**
     * Verify scanning of application.xml descriptors.
     */
    @Test
    public void applicationXml() {
        File webXml = new File(getClassesDirectory(XmlDescriptorScannerPluginIT.class), "META-INF/application.xml");
        store.beginTransaction();
        Scanner scanner = getScanner();
        ApplicationXmlDescriptor descriptor = scanner.scan(webXml, "/META-INF/application.xml", EnterpriseApplicationScope.EAR);
        assertThat(descriptor.getVersion(), equalTo("6"));
        store.commitTransaction();
    }

}
