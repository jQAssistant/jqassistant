package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.javaee6.api.model.AsyncSupportedDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.DescriptionDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.DispatcherDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.DisplayNameDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.FilterDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.FilterMappingDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.IconDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.ListenerDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.MultipartConfigDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.ParamValueDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.RunAsDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.SecurityRoleRefDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.ServletDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.ServletMappingDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.SessionConfigDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.UrlPatternDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebXmlDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;
import com.sun.java.xml.ns.javaee.DescriptionType;
import com.sun.java.xml.ns.javaee.DispatcherType;
import com.sun.java.xml.ns.javaee.DisplayNameType;
import com.sun.java.xml.ns.javaee.FilterMappingType;
import com.sun.java.xml.ns.javaee.FilterNameType;
import com.sun.java.xml.ns.javaee.FilterType;
import com.sun.java.xml.ns.javaee.FullyQualifiedClassType;
import com.sun.java.xml.ns.javaee.IconType;
import com.sun.java.xml.ns.javaee.JspFileType;
import com.sun.java.xml.ns.javaee.ListenerType;
import com.sun.java.xml.ns.javaee.MultipartConfigType;
import com.sun.java.xml.ns.javaee.ObjectFactory;
import com.sun.java.xml.ns.javaee.ParamValueType;
import com.sun.java.xml.ns.javaee.PathType;
import com.sun.java.xml.ns.javaee.RoleNameType;
import com.sun.java.xml.ns.javaee.RunAsType;
import com.sun.java.xml.ns.javaee.SecurityRoleRefType;
import com.sun.java.xml.ns.javaee.ServletMappingType;
import com.sun.java.xml.ns.javaee.ServletNameType;
import com.sun.java.xml.ns.javaee.ServletType;
import com.sun.java.xml.ns.javaee.SessionConfigType;
import com.sun.java.xml.ns.javaee.TrueFalseType;
import com.sun.java.xml.ns.javaee.UrlPatternType;
import com.sun.java.xml.ns.javaee.WebAppType;
import com.sun.java.xml.ns.javaee.XsdIntegerType;
import com.sun.java.xml.ns.javaee.XsdStringType;

/**
 * Scanner plugin for the content of web application XML descriptors (i.e.
 * WEB-INF/web.xml)
 */
public class WebXmlScannerPlugin extends AbstractWarResourceScannerPlugin<FileResource, WebXmlDescriptor> {

    private JAXBContext jaxbContext;

    @Override
    protected void initialize() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context.", e);
        }
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return WebApplicationScope.WAR.equals(scope) && "/WEB-INF/web.xml".equals(path);
    }

    @Override
    public WebXmlDescriptor scan(FileResource item, String path, JavaClassesDirectoryDescriptor classesDirectory, Scanner scanner) throws IOException {
        WebAppType webAppType;
        try (InputStream stream = item.createStream()) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            webAppType = unmarshaller.unmarshal(new StreamSource(stream), WebAppType.class).getValue();
        } catch (JAXBException e) {
            throw new IOException("Cannot read " + path, e);
        }
        Store store = scanner.getContext().getStore();
        WebXmlDescriptor webXmlDescriptor = store.create(WebXmlDescriptor.class);
        webXmlDescriptor.setVersion(webAppType.getVersion());
        Map<String, ServletDescriptor> servlets = new HashMap<>();
        for (JAXBElement<?> jaxbElement : webAppType.getModuleNameOrDescriptionAndDisplayName()) {
            Object value = jaxbElement.getValue();
            if (value instanceof ServletMappingType) {
                ServletMappingDescriptor servletMappingDescriptor = createServletMapping((ServletMappingType) value, store);
                webXmlDescriptor.getServletMappings().add(servletMappingDescriptor);
            } else if (value instanceof SessionConfigType) {
                SessionConfigDescriptor sessionConfig = createSessionConfig((SessionConfigType) value, store);
                webXmlDescriptor.setSessionConfig(sessionConfig);
            } else if (value instanceof FilterType) {
                FilterType filterType = (FilterType) value;
                FilterDescriptor filterDescriptor = createFilter(filterType, scanner.getContext());
                webXmlDescriptor.getFilters().add(filterDescriptor);
            } else if (value instanceof FilterMappingType) {
                FilterMappingDescriptor filterMapping = createFilterMapping((FilterMappingType) value, servlets, store);
                webXmlDescriptor.getFilterMappings().add(filterMapping);
            } else if (value instanceof ServletType) {
                ServletDescriptor servletDescriptor = createServlet((ServletType) value, servlets, scanner.getContext());
                webXmlDescriptor.getServlets().add(servletDescriptor);
            } else if (value instanceof ListenerType) {
                ListenerType listenerType = (ListenerType) value;
                ListenerDescriptor listenerDescriptor = store.create(ListenerDescriptor.class);
                for (DescriptionType descriptionType : listenerType.getDescription()) {
                    listenerDescriptor.getDescriptions().add(createDescription(descriptionType, store));
                }
                for (DisplayNameType displayNameType : listenerType.getDisplayName()) {
                    listenerDescriptor.getDisplayNames().add(createDisplayName(displayNameType, store));
                }
                for (IconType iconType : listenerType.getIcon()) {
                    listenerDescriptor.getIcons().add(createIcon(iconType, store));
                }
                TypeResolver typeResolver = scanner.getContext().peek(TypeResolver.class);
                FullyQualifiedClassType listenerClass = listenerType.getListenerClass();
                TypeCache.CachedType<TypeDescriptor> listenerClassDescriptor = typeResolver.resolve(listenerClass.getValue(), scanner.getContext());
                listenerDescriptor.setType(listenerClassDescriptor.getTypeDescriptor());
                webXmlDescriptor.getListeners().add(listenerDescriptor);
            }
        }
        return webXmlDescriptor;
    }

    /**
     * Create a filter descriptor.
     * 
     * @param filterType
     *            The XML filter type.
     * @param context
     *            The scanner context.
     * @return The filter descriptor.
     */
    private FilterDescriptor createFilter(FilterType filterType, ScannerContext context) {
        Store store = context.getStore();
        FilterDescriptor filterDescriptor = store.create(FilterDescriptor.class);
        setAsyncSupported(filterDescriptor, filterType.getAsyncSupported());
        for (DescriptionType descriptionType : filterType.getDescription()) {
            filterDescriptor.getDescriptions().add(createDescription(descriptionType, store));
        }
        for (DisplayNameType displayNameType : filterType.getDisplayName()) {
            filterDescriptor.getDisplayNames().add(createDisplayName(displayNameType, store));
        }
        FullyQualifiedClassType filterClass = filterType.getFilterClass();
        if (filterClass != null) {
            TypeResolver typeResolver = context.peek(TypeResolver.class);
            TypeCache.CachedType<TypeDescriptor> filterClassDescriptor = typeResolver.resolve(filterClass.getValue(), context);
            filterDescriptor.setType(filterClassDescriptor.getTypeDescriptor());
        }
        FilterNameType filterName = filterType.getFilterName();
        if (filterName != null) {
            filterDescriptor.setName(filterName.getValue());
        }
        for (IconType iconType : filterType.getIcon()) {
            IconDescriptor iconDescriptor = createIcon(iconType, store);
            filterDescriptor.getIcons().add(iconDescriptor);
        }
        for (ParamValueType paramValueType : filterType.getInitParam()) {
            ParamValueDescriptor paramValueDescriptor = createParamValue(paramValueType, store);
            filterDescriptor.getInitParams().add(paramValueDescriptor);
        }
        return filterDescriptor;
    }

    /**
     * Create a filter mapping descriptor.
     * 
     * @param filterMappingType
     *            The XML filter mapping type.
     * @param servlets
     *            The map of known servlets.
     * @param store
     *            The store.
     * @return The filter mapping descriptor.
     */
    private FilterMappingDescriptor createFilterMapping(FilterMappingType filterMappingType, Map<String, ServletDescriptor> servlets, Store store) {
        FilterMappingDescriptor filterMappingDescriptor = store.create(FilterMappingDescriptor.class);
        FilterNameType filterName = filterMappingType.getFilterName();
        filterMappingDescriptor.setFilterName(filterName.getValue());
        for (Object urlPatternOrServletName : filterMappingType.getUrlPatternOrServletName()) {
            if (urlPatternOrServletName instanceof UrlPatternType) {
                UrlPatternType urlPatternType = (UrlPatternType) urlPatternOrServletName;
                UrlPatternDescriptor urlPatternDescriptor = store.create(UrlPatternDescriptor.class);
                urlPatternDescriptor.setValue(urlPatternType.getValue());
                filterMappingDescriptor.getUrlPatterns().add(urlPatternDescriptor);
            } else if (urlPatternOrServletName instanceof ServletNameType) {
                ServletNameType servletNameType = (ServletNameType) urlPatternOrServletName;
                ServletDescriptor servletDescriptor = getOrCreateServletDescriptor(servletNameType, servlets, store);
                filterMappingDescriptor.setServlet(servletDescriptor);
            }
        }
        for (DispatcherType dispatcherType : filterMappingType.getDispatcher()) {
            DispatcherDescriptor dispatcherDescriptor = store.create(DispatcherDescriptor.class);
            dispatcherDescriptor.setValue(dispatcherType.getValue());
            filterMappingDescriptor.getDispatchers().add(dispatcherDescriptor);
        }

        return filterMappingDescriptor;
    }

    /**
     * Create a servlet descriptor.
     * 
     * @param servletType
     *            The XML servlet type.
     * @param servlets
     *            The map of known servlets.
     * @param context
     *            The scanner context.
     * @return The servlet descriptor.
     */
    private ServletDescriptor createServlet(ServletType servletType, Map<String, ServletDescriptor> servlets, ScannerContext context) {
        Store store = context.getStore();
        ServletDescriptor servletDescriptor = getOrCreateServletDescriptor(servletType.getServletName(), servlets, store);
        setAsyncSupported(servletDescriptor, servletType.getAsyncSupported());
        for (DescriptionType descriptionType : servletType.getDescription()) {
            servletDescriptor.getDescriptions().add(createDescription(descriptionType, store));
        }
        for (DisplayNameType displayNameType : servletType.getDisplayName()) {
            servletDescriptor.getDisplayNames().add(createDisplayName(displayNameType, store));
        }
        TrueFalseType enabled = servletType.getEnabled();
        if (enabled != null) {
            servletDescriptor.setEnabled(enabled.isValue());
        }
        for (IconType iconType : servletType.getIcon()) {
            IconDescriptor iconDescriptor = createIcon(iconType, store);
            servletDescriptor.getIcons().add(iconDescriptor);
        }
        for (ParamValueType paramValueType : servletType.getInitParam()) {
            ParamValueDescriptor paramValueDescriptor = createParamValue(paramValueType, store);
            servletDescriptor.getInitParams().add(paramValueDescriptor);
        }
        JspFileType jspFile = servletType.getJspFile();
        if (jspFile != null) {
            servletDescriptor.setJspFile(jspFile.getValue());
        }
        String loadOnStartup = servletType.getLoadOnStartup();
        if (loadOnStartup != null) {
            servletDescriptor.setLoadOnStartup(Boolean.valueOf(loadOnStartup.toUpperCase()));
        }
        MultipartConfigType multipartConfig = servletType.getMultipartConfig();
        if (multipartConfig != null) {
            MultipartConfigDescriptor multipartConfigDescriptor = store.create(MultipartConfigDescriptor.class);
            BigInteger fileSizeThreshold = multipartConfig.getFileSizeThreshold();
            if (fileSizeThreshold != null) {
                multipartConfigDescriptor.setFileSizeThreshold(fileSizeThreshold.longValue());
            }
            multipartConfigDescriptor.setLocation(multipartConfig.getLocation());
            multipartConfigDescriptor.setMaxFileSize(multipartConfig.getMaxFileSize());
            multipartConfigDescriptor.setMaxRequestSize(multipartConfig.getMaxRequestSize());
            servletDescriptor.setMultipartConfig(multipartConfigDescriptor);
        }
        RunAsType runAs = servletType.getRunAs();
        if (runAs != null) {
            RunAsDescriptor runAsDescriptor = store.create(RunAsDescriptor.class);
            for (DescriptionType descriptionType : runAs.getDescription()) {
                DescriptionDescriptor descriptionDescriptor = createDescription(descriptionType, store);
                runAsDescriptor.getDescriptions().add(descriptionDescriptor);
            }
            RoleNameType roleName = runAs.getRoleName();
            if (roleName != null) {
                runAsDescriptor.setRoleName(roleName.getValue());
            }
            servletDescriptor.setRunAs(runAsDescriptor);
        }
        for (SecurityRoleRefType securityRoleRefType : servletType.getSecurityRoleRef()) {
            SecurityRoleRefDescriptor securityRoleRefDescriptor = store.create(SecurityRoleRefDescriptor.class);
            securityRoleRefDescriptor.setRoleName(securityRoleRefType.getRoleName().getValue());
            for (DescriptionType descriptionType : securityRoleRefType.getDescription()) {
                DescriptionDescriptor descriptionDescriptor = createDescription(descriptionType, store);
                securityRoleRefDescriptor.getDescriptions().add(descriptionDescriptor);
            }
            RoleNameType roleLink = securityRoleRefType.getRoleLink();
            if (roleLink != null) {
                securityRoleRefDescriptor.setRoleLink(roleLink.getValue());
            }
            servletDescriptor.getSecurityRoleRefs().add(securityRoleRefDescriptor);
        }
        FullyQualifiedClassType servletClass = servletType.getServletClass();
        if (servletClass != null) {
            TypeCache.CachedType<TypeDescriptor> servletClassType = context.peek(TypeResolver.class).resolve(servletClass.getValue(), context);
            servletDescriptor.setType(servletClassType.getTypeDescriptor());
        }
        return servletDescriptor;
    }

    /**
     * Get or create a servlet descriptor.
     * 
     * @param servletType
     *            The XML servlet name type.
     * @param servlets
     *            The map of known servlets.
     * @param store
     *            The store.
     * @return The servlet descriptor.
     */
    private ServletDescriptor getOrCreateServletDescriptor(ServletNameType servletType, Map<String, ServletDescriptor> servlets, Store store) {
        String servletName = servletType.getValue();
        ServletDescriptor servletDescriptor = servlets.get(servletName);
        if (servletDescriptor == null) {
            store.create(ServletDescriptor.class);
            servletDescriptor.setName(servletName);
        }
        return servletDescriptor;
    }

    /**
     * Create a param value descriptor.
     * 
     * @param paramValueType
     *            The XML param value type.
     * @param store
     *            The store.
     * @return The param value descriptor.
     */
    private ParamValueDescriptor createParamValue(ParamValueType paramValueType, Store store) {
        ParamValueDescriptor paramValueDescriptor = store.create(ParamValueDescriptor.class);
        for (DescriptionType descriptionType : paramValueType.getDescription()) {
            DescriptionDescriptor descriptionDescriptor = createDescription(descriptionType, store);
            paramValueDescriptor.getDescriptions().add(descriptionDescriptor);
        }
        paramValueDescriptor.setParamName(paramValueType.getParamName().getValue());
        XsdStringType paramValue = paramValueType.getParamValue();
        if (paramValue != null) {
            paramValueDescriptor.setParamValue(paramValue.getValue());
        }
        return paramValueDescriptor;
    }

    /**
     * Create an icon descriptor.
     * 
     * @param iconType
     *            The XML icon type.
     * @param store
     *            The store
     * @return The icon descriptor.
     */
    private IconDescriptor createIcon(IconType iconType, Store store) {
        IconDescriptor iconDescriptor = store.create(IconDescriptor.class);
        iconDescriptor.setLang(iconType.getLang());
        PathType largeIcon = iconType.getLargeIcon();
        if (largeIcon != null) {
            iconDescriptor.setLargeIcon(largeIcon.getValue());
        }
        PathType smallIcon = iconType.getSmallIcon();
        if (smallIcon != null) {
            iconDescriptor.setSmallIcon(smallIcon.getValue());
        }
        return iconDescriptor;
    }

    /**
     * Create a display name descriptor.
     * 
     * @param displayNameType
     *            The XML display name type.
     * @param store
     *            The store.
     * @return The display name descriptor.
     */
    private DisplayNameDescriptor createDisplayName(DisplayNameType displayNameType, Store store) {
        DisplayNameDescriptor displayNameDescriptor = store.create(DisplayNameDescriptor.class);
        displayNameDescriptor.setLang(displayNameType.getLang());
        displayNameDescriptor.setValue(displayNameType.getValue());
        return displayNameDescriptor;
    }

    /**
     * Set the value for an async supported on the given descriptor.
     * 
     * @param asyncSupportedDescriptor
     *            The async supported descriptor.
     * @param asyncSupported
     *            The value.
     */
    private void setAsyncSupported(AsyncSupportedDescriptor asyncSupportedDescriptor, TrueFalseType asyncSupported) {
        if (asyncSupported != null) {
            asyncSupportedDescriptor.setAsyncSupported(asyncSupported.isValue());
        }
    }

    /**
     * Create a description descriptor.
     * 
     * @param descriptionType
     *            The XML description type.
     * @param store
     *            The store.
     * @return The description descriptor.
     */
    private DescriptionDescriptor createDescription(DescriptionType descriptionType, Store store) {
        DescriptionDescriptor descriptionDescriptor = store.create(DescriptionDescriptor.class);
        descriptionDescriptor.setLang(descriptionType.getLang());
        descriptionDescriptor.setValue(descriptionType.getValue());
        return descriptionDescriptor;
    }

    /**
     * Create a servlet mapping descriptor.
     * 
     * @param servletMappingType
     *            The XML servlet mapping type.
     * @param store
     *            The store.
     * @return The servlet mapping descriptor.
     */
    private ServletMappingDescriptor createServletMapping(ServletMappingType servletMappingType, Store store) {
        ServletMappingDescriptor servletMappingDescriptor = store.create(ServletMappingDescriptor.class);
        ServletNameType servletName = servletMappingType.getServletName();
        servletMappingDescriptor.setServletName(servletName.getValue());
        for (UrlPatternType urlPatternType : servletMappingType.getUrlPattern()) {
            UrlPatternDescriptor urlPatternDescriptor = store.create(UrlPatternDescriptor.class);
            urlPatternDescriptor.setValue(urlPatternType.getValue());
            servletMappingDescriptor.getUrlPatterns().add(urlPatternDescriptor);
        }
        return servletMappingDescriptor;
    }

    /**
     * Create a session config descriptor.
     * 
     * @param sessionConfigType
     *            The XML session config type.
     * @param store
     *            The store.
     * @return The session config descriptor.
     */
    private SessionConfigDescriptor createSessionConfig(SessionConfigType sessionConfigType, Store store) {
        SessionConfigDescriptor sessionConfigDescriptor = store.create(SessionConfigDescriptor.class);
        XsdIntegerType sessionTimeout = sessionConfigType.getSessionTimeout();
        if (sessionTimeout != null) {
            sessionConfigDescriptor.setSessionTimeout(sessionTimeout.getValue().intValue());
        }
        return sessionConfigDescriptor;
    }
}
