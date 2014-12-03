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
    private SessionConfigDescriptor sessionConfigDescriptor;

    @Mock
    private ParamValueDescriptor contextParamDescriptor;

    @Mock
    private List<ParamValueDescriptor> contextParamDescriptors;

    // Servlet
    @Mock
    private List<ServletDescriptor> servletDescriptors;

    @Mock
    private ServletDescriptor servletDescriptor;

    @Mock
    private DescriptionDescriptor servletDescriptionDescriptor;

    @Mock
    private List<DescriptionDescriptor> servletDescriptionDescriptors;

    @Mock
    private DisplayNameDescriptor servletDisplayNameDescriptor;

    @Mock
    private List<DisplayNameDescriptor> servletDisplayNameDescriptors;

    @Mock
    private IconDescriptor servletIconDescriptor;

    @Mock
    private List<IconDescriptor> servletIconDescriptors;

    @Mock
    private RunAsDescriptor runAsDescriptor;

    @Mock
    private DescriptionDescriptor servletRunAsDescriptionDescriptor;

    @Mock
    private List<DescriptionDescriptor> servletRunAsDescriptionDescriptors;

    @Mock
    private ParamValueDescriptor servletInitParamDescriptor;

    @Mock
    private List<ParamValueDescriptor> servletInitParamDescriptors;

    @Mock
    private ClassFileDescriptor servletClassDescriptor;

    @Mock
    private TypeCache.CachedType<TypeDescriptor> cachedServletClassDescriptor;

    // Servlet Mapping

    @Mock
    private ServletMappingDescriptor servletMappingDescriptor;

    @Mock
    private List<ServletMappingDescriptor> servletMappingDescriptors;

    @Mock
    private List<ServletMappingDescriptor> allServletMappingDescriptors;

    @Mock
    private UrlPatternDescriptor servletUrlMappingDescriptor;

    @Mock
    private List<UrlPatternDescriptor> servletUrlMappingDescriptors;

    // Filter

    @Mock
    private FilterDescriptor filterDescriptor;

    @Mock
    private List<FilterDescriptor> filterDescriptors;

    @Mock
    private DescriptionDescriptor filterDescriptionDescriptor;

    @Mock
    private List<DescriptionDescriptor> filterDescriptionDescriptors;

    @Mock
    private DisplayNameDescriptor filterDisplayNameDescriptor;

    @Mock
    private List<DisplayNameDescriptor> filterDisplayNameDescriptors;

    @Mock
    private IconDescriptor filterIconDescriptor;

    @Mock
    private List<IconDescriptor> filterIconDescriptors;

    @Mock
    private ParamValueDescriptor filterInitParamDescriptor;

    @Mock
    private List<ParamValueDescriptor> filterInitParamDescriptors;

    @Mock
    private TypeCache.CachedType<TypeDescriptor> cachedFilterClassDescriptor;

    @Mock
    private ClassFileDescriptor filterClassDescriptor;

    // Filter Mapping

    @Mock
    private FilterMappingDescriptor filterMappingDescriptor;

    @Mock
    private List<FilterMappingDescriptor> filterMappingDescriptors;

    @Mock
    private List<FilterMappingDescriptor> allFilterMappingDescriptors;

    @Mock
    private UrlPatternDescriptor filterUrlMappingDescriptor;

    @Mock
    private List<UrlPatternDescriptor> filterUrlMappingDescriptors;

    // Listener

    @Mock
    private TypeCache.CachedType<TypeDescriptor> cachedListenerClassDescriptor;

    @Mock
    private ClassFileDescriptor listenerClassDescriptor;

    @Mock
    private ListenerDescriptor listenerDescriptor;

    @Mock
    private List<ListenerDescriptor> allListenerDescriptors;

    @Test
    public void webXml() throws IOException {
        when(scanner.getContext()).thenReturn(scannerContext);
        when(scannerContext.getStore()).thenReturn(store);
        when(scannerContext.peek(WebApplicationArchiveDescriptor.class)).thenReturn(warDescriptor);
        when(scannerContext.peek(TypeResolver.class)).thenReturn(typeResolver);

        FileResource fileResource = mock(FileResource.class);
        when(fileResource.createStream()).thenReturn(WebXmlScannerPlugin.class.getResourceAsStream("/WEB-INF/web.xml"));

        when(store.create(WebXmlDescriptor.class)).thenReturn(webXmlDescriptor);
        when(webXmlDescriptor.getContextParams()).thenReturn(contextParamDescriptors);
        when(webXmlDescriptor.getServlets()).thenReturn(servletDescriptors);
        when(webXmlDescriptor.getServletMappings()).thenReturn(allServletMappingDescriptors);
        when(webXmlDescriptor.getFilterMappings()).thenReturn(allFilterMappingDescriptors);
        when(webXmlDescriptor.getListeners()).thenReturn(allListenerDescriptors);

        when(store.create(SessionConfigDescriptor.class)).thenReturn(sessionConfigDescriptor);

        // Servlet
        when(store.create(ServletDescriptor.class)).thenReturn(servletDescriptor);
        when(servletDescriptor.getName()).thenReturn("TestServlet");
        when(servletDescriptor.getDescriptions()).thenReturn(servletDescriptionDescriptors);
        when(servletDescriptor.getDisplayNames()).thenReturn(servletDisplayNameDescriptors);
        when(servletDescriptor.getIcons()).thenReturn(servletIconDescriptors);
        when(servletDescriptor.getInitParams()).thenReturn(servletInitParamDescriptors);
        when(servletDescriptor.getMappings()).thenReturn(servletMappingDescriptors);

        when(store.create(RunAsDescriptor.class)).thenReturn(runAsDescriptor);
        when(runAsDescriptor.getDescriptions()).thenReturn(servletRunAsDescriptionDescriptors);

        when(store.create(DescriptionDescriptor.class)).thenReturn(servletDescriptionDescriptor, servletRunAsDescriptionDescriptor,
                filterDescriptionDescriptor, null);
        when(store.create(DisplayNameDescriptor.class)).thenReturn(servletDisplayNameDescriptor, filterDisplayNameDescriptor, null);
        when(store.create(IconDescriptor.class)).thenReturn(servletIconDescriptor, filterIconDescriptor, null);

        when(store.create(ParamValueDescriptor.class)).thenReturn(contextParamDescriptor, servletInitParamDescriptor, filterInitParamDescriptor, null);
        when(typeResolver.resolve("com.buschmais.jqassistant.TestServlet", scannerContext)).thenReturn(cachedServletClassDescriptor);
        when(cachedServletClassDescriptor.getTypeDescriptor()).thenReturn(servletClassDescriptor);

        // Servlet Mapping
        when(store.create(ServletMappingDescriptor.class)).thenReturn(servletMappingDescriptor);
        when(store.create(UrlPatternDescriptor.class)).thenReturn(servletUrlMappingDescriptor, filterUrlMappingDescriptor, null);
        when(servletMappingDescriptor.getUrlPatterns()).thenReturn(servletUrlMappingDescriptors);

        // Filter
        when(store.create(FilterDescriptor.class)).thenReturn(filterDescriptor);
        when(webXmlDescriptor.getFilters()).thenReturn(filterDescriptors);
        when(typeResolver.resolve("com.buschmais.jqassistant.TestFilter", scannerContext)).thenReturn(cachedFilterClassDescriptor);
        when(cachedFilterClassDescriptor.getTypeDescriptor()).thenReturn(filterClassDescriptor);
        when(filterDescriptor.getDescriptions()).thenReturn(filterDescriptionDescriptors);
        when(filterDescriptor.getDisplayNames()).thenReturn(filterDisplayNameDescriptors);
        when(filterDescriptor.getIcons()).thenReturn(filterIconDescriptors);
        when(filterDescriptor.getInitParams()).thenReturn(filterInitParamDescriptors);
        when(filterDescriptor.getMappings()).thenReturn(filterMappingDescriptors);

        // Filter Mapping
        when(store.create(FilterMappingDescriptor.class)).thenReturn(filterMappingDescriptor);
        when(filterMappingDescriptor.getUrlPatterns()).thenReturn(filterUrlMappingDescriptors);

        // Listener
        when(typeResolver.resolve("com.buschmais.jqassistant.TestListener", scannerContext)).thenReturn(cachedListenerClassDescriptor);
        when(cachedListenerClassDescriptor.getTypeDescriptor()).thenReturn(listenerClassDescriptor);
        when(store.create(ListenerDescriptor.class)).thenReturn(listenerDescriptor);

        WebXmlScannerPlugin scannerPlugin = new WebXmlScannerPlugin();
        scannerPlugin.initialize(Collections.<String, Object> emptyMap());
        scannerPlugin.scan(fileResource, "/WEB-INF/web.xml", WebApplicationScope.WAR, scanner);

        verify(store).create(WebXmlDescriptor.class);
        verify(webXmlDescriptor).setVersion("3.0");
        verify(webXmlDescriptor).setSessionConfig(sessionConfigDescriptor);
        verify(contextParamDescriptors).add(contextParamDescriptor);
        verify(contextParamDescriptor).setName("contextParam");
        verify(contextParamDescriptor).setValue("contextParamValue");
        verify(sessionConfigDescriptor).setSessionTimeout(30);

        verifyServlet();
        verifyServletMapping();
        verifyFilter();
        verifyFilterMapping();
        verifyListener();
    }

    private void verifyListener() {
        verify(allListenerDescriptors).add(listenerDescriptor);
        verify(listenerDescriptor).setType(listenerClassDescriptor);

    }

    private void verifyFilterMapping() {
        verify(store).create(FilterMappingDescriptor.class);
        verify(filterMappingDescriptors).add(filterMappingDescriptor);
        verify(filterUrlMappingDescriptors).add(filterUrlMappingDescriptor);
        verify(filterUrlMappingDescriptor).setValue("/*");
    }

    private void verifyFilter() {
        verify(store).create(FilterDescriptor.class);
        verify(filterDescriptors).add(filterDescriptor);
        verify(filterDescriptor).setName("TestFilter");
        verify(filterDescriptor).setType(filterClassDescriptor);
        verifyDescription(filterDescriptionDescriptors, filterDescriptionDescriptor, "en", "Test Filter Description");
        verifyDisplayName(filterDisplayNameDescriptors, filterDisplayNameDescriptor, "en", "Test Filter");
        verifyIcon(filterIconDescriptors, filterIconDescriptor, "filterIcon-small.png", "filterIcon-large.png");
        verifyInitParam(filterInitParamDescriptors, filterInitParamDescriptor, "filterParam", "filterParamValue");
        verify(filterMappingDescriptors).add(filterMappingDescriptor);
    }

    private void verifyServletMapping() {
        verify(store).create(ServletMappingDescriptor.class);
        verify(allServletMappingDescriptors).add(servletMappingDescriptor);
        verify(store, times(2)).create(UrlPatternDescriptor.class);
        verify(servletUrlMappingDescriptors).add(servletUrlMappingDescriptor);
        verify(servletUrlMappingDescriptor).setValue("/*");
    }

    private void verifyServlet() {
        verify(servletDescriptors).add(servletDescriptor);
        verify(servletDescriptor).setName("TestServlet");
        verify(servletDescriptor).setType(servletClassDescriptor);
        verifyDescription(servletDescriptionDescriptors, servletDescriptionDescriptor, "en", "Test Servlet Description");
        verifyDisplayName(servletDisplayNameDescriptors, servletDisplayNameDescriptor, "en", "Test Servlet");
        verifyIcon(servletIconDescriptors, servletIconDescriptor, "servletIcon-small.png", "servletIcon-large.png");
        verifyInitParam(servletInitParamDescriptors, servletInitParamDescriptor, "servletParam", "servletParamValue");
        verify(servletMappingDescriptors).add(servletMappingDescriptor);
        verify(servletDescriptor).setLoadOnStartup("1");
        verify(servletDescriptor).setEnabled(true);
        verify(servletDescriptor).setAsyncSupported(true);
        verify(servletDescriptor).setRunAs(runAsDescriptor);
        verify(runAsDescriptor).setRoleName("Admin");
        verify(servletRunAsDescriptionDescriptors).add(servletRunAsDescriptionDescriptor);
        verify(servletRunAsDescriptionDescriptor).setLang("en");
        verify(servletRunAsDescriptionDescriptor).setValue("Administrator");
    }

    private void verifyInitParam(List<ParamValueDescriptor> descriptors, ParamValueDescriptor descriptor, String name, String value) {
        verify(descriptors).add(descriptor);
        verify(descriptor).setName(name);
        verify(descriptor).setValue(value);
    }

    private void verifyIcon(List<IconDescriptor> descriptors, IconDescriptor descriptor, String smallIcon, String largeIcon) {
        verify(descriptors).add(descriptor);
        verify(descriptor).setSmallIcon(smallIcon);
        verify(descriptor).setLargeIcon(largeIcon);
    }

    private void verifyDisplayName(List<DisplayNameDescriptor> descriptors, DisplayNameDescriptor descriptor, String lang, String value) {
        verify(descriptors).add(descriptor);
        verify(descriptor).setLang(lang);
        verify(descriptor).setValue(value);
    }

    private void verifyDescription(List<DescriptionDescriptor> descriptors, DescriptionDescriptor descriptor, String lang, String value) {
        verify(descriptors).add(descriptor);
        verify(descriptor).setLang(lang);
        verify(descriptor).setValue(value);
    }
}
