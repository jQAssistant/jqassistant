package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.javaee6.api.model.*;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;
import com.buschmais.jqassistant.plugin.javaee6.impl.scanner.WebXmlScannerPlugin;

@RunWith(MockitoJUnitRunner.class)
public class WebXmlScannerPluginTest {

    @Mock
    private Scanner scanner;

    @Mock
    private ScannerContext scannerContext;

    @Mock
    private Store store;

    @Mock
    private TypeResolver typeResolver;

    @Mock
    private WebApplicationArchiveDescriptor warDescriptor;

    @Mock
    private WebXmlDescriptor webXmlDescriptor;

    @Mock
    private List<ServletDescriptor> servletDescriptors;

    @Mock
    private SessionConfigDescriptor sessionConfigDescriptor;

    // Servlet

    @Mock
    private ServletDescriptor servletDescriptor;

    @Mock
    private DescriptionDescriptor servletDescriptionDescriptor;

    @Mock
    private DisplayNameDescriptor servletDisplayNameDescriptor;

    @Mock
    private IconDescriptor servletIconDescriptor;

    @Mock
    private ParamValueDescriptor servletInitParamDescriptor;

    @Mock
    private ServletMappingDescriptor servletMappingDescriptor;

    @Mock
    private UrlPatternDescriptor servletUrlMappingDescriptor;

    @Mock
    private TypeCache.CachedType<TypeDescriptor> cachedServletClassDescriptor;

    @Mock
    private ClassFileDescriptor servletClassDescriptor;

    // Filter

    @Mock
    private FilterDescriptor filterDescriptor;

    @Mock
    private DescriptionDescriptor filterDescription;

    @Mock
    private ParamValueDescriptor filterInitParamDescriptor;

    @Mock
    private TypeCache.CachedType<TypeDescriptor> cachedFilterClassDescriptor;

    @Mock
    private ClassFileDescriptor filterClassDescriptor;

    @Mock
    private FilterMappingDescriptor filterMappingDescriptor;

    @Mock
    private UrlPatternDescriptor filterUrlMappingDescriptor;

    // Listener

    @Mock
    private TypeCache.CachedType<TypeDescriptor> cachedListenerClassDescriptor;

    @Mock
    private ClassFileDescriptor listenerClassDescriptor;

    @Mock
    private ListenerDescriptor listenerDescriptor;

    @Test
    public void webXml() throws IOException {
        when(scanner.getContext()).thenReturn(scannerContext);
        when(scannerContext.getStore()).thenReturn(store);
        when(scannerContext.peek(WebApplicationArchiveDescriptor.class)).thenReturn(warDescriptor);
        when(scannerContext.peek(TypeResolver.class)).thenReturn(typeResolver);

        WebXmlScannerPlugin scannerPlugin = new WebXmlScannerPlugin();
        scannerPlugin.initialize(Collections.<String, Object> emptyMap());
        FileResource fileResource = mock(FileResource.class);
        when(fileResource.createStream()).thenReturn(WebXmlScannerPlugin.class.getResourceAsStream("/WEB-INF/web.xml"));

        when(store.create(WebXmlDescriptor.class)).thenReturn(webXmlDescriptor);
        when(webXmlDescriptor.getServlets()).thenReturn(servletDescriptors);

        when(store.create(SessionConfigDescriptor.class)).thenReturn(sessionConfigDescriptor);

        when(store.create(ServletDescriptor.class)).thenReturn(servletDescriptor);
        when(servletDescriptor.getName()).thenReturn("TestServlet");
        when(store.create(DescriptionDescriptor.class)).thenReturn(servletDescriptionDescriptor);
        when(store.create(DisplayNameDescriptor.class)).thenReturn(servletDisplayNameDescriptor);
        when(store.create(IconDescriptor.class)).thenReturn(servletIconDescriptor);

        when(store.create(ParamValueDescriptor.class)).thenReturn(servletInitParamDescriptor, filterInitParamDescriptor, null);
        when(typeResolver.resolve("com.buschmais.jqassistant.TestServlet", scannerContext)).thenReturn(cachedServletClassDescriptor);
        when(cachedServletClassDescriptor.getTypeDescriptor()).thenReturn(servletClassDescriptor);
        when(store.create(ServletMappingDescriptor.class)).thenReturn(servletMappingDescriptor);
        when(store.create(UrlPatternDescriptor.class)).thenReturn(servletUrlMappingDescriptor, filterUrlMappingDescriptor, null);
        when(typeResolver.resolve("com.buschmais.jqassistant.TestFilter", scannerContext)).thenReturn(cachedFilterClassDescriptor);
        when(cachedFilterClassDescriptor.getTypeDescriptor()).thenReturn(filterClassDescriptor);
        when(typeResolver.resolve("com.buschmais.jqassistant.TestListener", scannerContext)).thenReturn(cachedListenerClassDescriptor);
        when(cachedListenerClassDescriptor.getTypeDescriptor()).thenReturn(listenerClassDescriptor);
        when(store.create(ListenerDescriptor.class)).thenReturn(listenerDescriptor);
        when(store.create(FilterDescriptor.class)).thenReturn(filterDescriptor);
        when(store.create(FilterMappingDescriptor.class)).thenReturn(filterMappingDescriptor);

        scannerPlugin.scan(fileResource, "/WEB-INF/web.xml", WebApplicationScope.WAR, scanner);

        verify(store).create(WebXmlDescriptor.class);
        verify(webXmlDescriptor).setVersion("3.0");
        verify(webXmlDescriptor).setSessionConfig(sessionConfigDescriptor);
        verify(sessionConfigDescriptor).setSessionTimeout(30);
        verify(servletDescriptors).add(servletDescriptor);
        // verify()
    }
}
