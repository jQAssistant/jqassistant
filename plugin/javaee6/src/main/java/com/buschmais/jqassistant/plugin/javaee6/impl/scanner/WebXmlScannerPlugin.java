package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
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
import com.buschmais.jqassistant.plugin.javaee6.api.model.*;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;
import com.sun.java.xml.ns.javaee.*;

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
                FilterMappingType filterMappingType = (FilterMappingType) value;
            } else if (value instanceof ServletType) {
                ServletDescriptor servletDescriptor = createServlet((ServletType) value, servlets, scanner.getContext());
                webXmlDescriptor.getServlets().add(servletDescriptor);
            }
        }
        return webXmlDescriptor;
    }

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
            }
        }
        return filterMappingDescriptor;
    }

    private ServletDescriptor createServlet(ServletType servletType, Map<String, ServletDescriptor> servlets, ScannerContext context) {
        Store store = context.getStore();
        ServletNameType servletNameType = servletType.getServletName();
        ServletDescriptor servletDescriptor = store.create(ServletDescriptor.class);
        if (servletNameType != null) {
            servletDescriptor.setName(servletNameType.getValue());
        }
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

    private DisplayNameDescriptor createDisplayName(DisplayNameType displayNameType, Store store) {
        DisplayNameDescriptor displayNameDescriptor = store.create(DisplayNameDescriptor.class);
        displayNameDescriptor.setLang(displayNameType.getLang());
        displayNameDescriptor.setValue(displayNameType.getValue());
        return displayNameDescriptor;
    }

    private void setAsyncSupported(AsyncSupportedDescriptor asyncSupportedDescriptor, TrueFalseType asyncSupported) {
        if (asyncSupported != null) {
            asyncSupportedDescriptor.setAsyncSupported(asyncSupported.isValue());
        }
    }

    private DescriptionDescriptor createDescription(DescriptionType descriptionType, Store store) {
        DescriptionDescriptor descriptionDescriptor = store.create(DescriptionDescriptor.class);
        descriptionDescriptor.setLang(descriptionType.getLang());
        descriptionDescriptor.setValue(descriptionType.getValue());
        return descriptionDescriptor;
    }

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

    private SessionConfigDescriptor createSessionConfig(SessionConfigType sessionConfigType, Store store) {
        SessionConfigDescriptor sessionConfigDescriptor = store.create(SessionConfigDescriptor.class);
        XsdIntegerType sessionTimeout = sessionConfigType.getSessionTimeout();
        if (sessionTimeout != null) {
            sessionConfigDescriptor.setSessionTimeout(sessionTimeout.getValue().intValue());
        }
        return sessionConfigDescriptor;
    }
}
