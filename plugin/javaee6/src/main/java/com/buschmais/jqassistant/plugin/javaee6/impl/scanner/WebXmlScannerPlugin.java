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
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.javaee6.api.model.*;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.sun.java.xml.ns.javaee.*;

/**
 * Scanner plugin for the content of web application XML descriptors (i.e.
 * WEB-INF/web.xml)
 */
@Requires(XmlFileDescriptor.class)
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
        XmlFileDescriptor xmlFileDescriptor = scanner.getContext().peek(XmlFileDescriptor.class);
        WebXmlDescriptor webXmlDescriptor = store.addDescriptorType(xmlFileDescriptor, WebXmlDescriptor.class);
        webXmlDescriptor.setVersion(webAppType.getVersion());
        Map<String, ServletDescriptor> servlets = new HashMap<>();
        Map<String, FilterDescriptor> filters = new HashMap<>();
        for (JAXBElement<?> jaxbElement : webAppType.getModuleNameOrDescriptionAndDisplayName()) {
            Object value = jaxbElement.getValue();
            if (value instanceof ParamValueType) {
                ParamValueDescriptor paramValue = createParamValue((ParamValueType) value, store);
                webXmlDescriptor.getContextParams().add(paramValue);
            } else if (value instanceof ErrorPageType) {
                ErrorPageDescriptor errorPageDescriptor = createErrorPage((ErrorPageType) value, scanner.getContext());
                webXmlDescriptor.getErrorPages().add(errorPageDescriptor);
            } else if (value instanceof ServletMappingType) {
                ServletMappingDescriptor servletMappingDescriptor = createServletMapping((ServletMappingType) value, servlets, store);
                webXmlDescriptor.getServletMappings().add(servletMappingDescriptor);
            } else if (value instanceof SessionConfigType) {
                SessionConfigDescriptor sessionConfig = createSessionConfig((SessionConfigType) value, store);
                webXmlDescriptor.setSessionConfig(sessionConfig);
            } else if (value instanceof FilterType) {
                FilterType filterType = (FilterType) value;
                FilterDescriptor filterDescriptor = createFilter(filterType, filters, scanner.getContext());
                webXmlDescriptor.getFilters().add(filterDescriptor);
            } else if (value instanceof FilterMappingType) {
                FilterMappingDescriptor filterMapping = createFilterMapping((FilterMappingType) value, filters, servlets, store);
                webXmlDescriptor.getFilterMappings().add(filterMapping);
            } else if (value instanceof ServletType) {
                ServletDescriptor servletDescriptor = createServlet((ServletType) value, servlets, scanner.getContext());
                webXmlDescriptor.getServlets().add(servletDescriptor);
            } else if (value instanceof ListenerType) {
                ListenerDescriptor listenerDescriptor = createListener((ListenerType) value, scanner.getContext());
                webXmlDescriptor.getListeners().add(listenerDescriptor);
            } else if (value instanceof SecurityConstraintType) {
                SecurityConstraintDescriptor securityConstraintDescriptor = createSecurityConstraint((SecurityConstraintType) value, store);
                webXmlDescriptor.getSecurityConstraints().add(securityConstraintDescriptor);
            } else if (value instanceof SecurityRoleType) {
                SecurityRoleDescriptor securityRoleDescriptor = XmlDescriptorHelper.createSecurityRole((SecurityRoleType) value, store);
                webXmlDescriptor.getSecurityRoles().add(securityRoleDescriptor);
            } else if (value instanceof LoginConfigType) {
                LoginConfigDescriptor loginConfigDescriptor = createLoginConfig((LoginConfigType) value, store);
                webXmlDescriptor.getLoginConfigs().add(loginConfigDescriptor);
            }
        }
        return webXmlDescriptor;
    }

    private LoginConfigDescriptor createLoginConfig(LoginConfigType loginConfigType, Store store) {
        LoginConfigDescriptor loginConfigDescriptor = store.create(LoginConfigDescriptor.class);
        AuthMethodType authMethod = loginConfigType.getAuthMethod();
        if (authMethod != null) {
            loginConfigDescriptor.setAuthMethod(authMethod.getValue());
        }
        FormLoginConfigType formLoginConfigType = loginConfigType.getFormLoginConfig();
        if (formLoginConfigType != null) {
            FormLoginConfigDescriptor formLoginConfigDescriptor = store.create(FormLoginConfigDescriptor.class);
            formLoginConfigDescriptor.setFormLoginPage(formLoginConfigType.getFormLoginPage().getValue());
            formLoginConfigDescriptor.setFormErrorPage(formLoginConfigType.getFormErrorPage().getValue());
            loginConfigDescriptor.setFormLoginConfig(formLoginConfigDescriptor);
        }
        com.sun.java.xml.ns.javaee.String realmName = loginConfigType.getRealmName();
        if (realmName != null) {
            loginConfigDescriptor.setRealmName(realmName.getValue());
        }
        return loginConfigDescriptor;
    }

    private SecurityConstraintDescriptor createSecurityConstraint(SecurityConstraintType securityConstraintType, Store store) {
        SecurityConstraintDescriptor securityConstraintDescriptor = store.create(SecurityConstraintDescriptor.class);
        for (DisplayNameType displayNameType : securityConstraintType.getDisplayName()) {
            securityConstraintDescriptor.getDisplayNames().add(XmlDescriptorHelper.createDisplayName(displayNameType, store));
        }
        UserDataConstraintType userDataConstraint = securityConstraintType.getUserDataConstraint();
        if (userDataConstraint != null) {
            UserDataConstraintDescriptor userDataConstraintDescriptor = store.create(UserDataConstraintDescriptor.class);
            userDataConstraintDescriptor.setTransportGuarantee(userDataConstraint.getTransportGuarantee().getValue());
            for (DescriptionType descriptionType : userDataConstraint.getDescription()) {
                userDataConstraintDescriptor.getDescriptions().add(XmlDescriptorHelper.createDescription(descriptionType, store));
            }
            securityConstraintDescriptor.getUserDataConstraints().add(userDataConstraintDescriptor);
        }
        AuthConstraintType authConstraint = securityConstraintType.getAuthConstraint();
        if (authConstraint != null) {
            AuthConstraintDescriptor authConstraintDescriptor = store.create(AuthConstraintDescriptor.class);
            for (DescriptionType descriptionType : authConstraint.getDescription()) {
                authConstraintDescriptor.getDescriptions().add(XmlDescriptorHelper.createDescription(descriptionType, store));
            }
            for (RoleNameType roleNameType : authConstraint.getRoleName()) {
                RoleNameDescriptor roleNameDescriptor = XmlDescriptorHelper.createRoleName(roleNameType, store);
                authConstraintDescriptor.getRoleNames().add(roleNameDescriptor);
            }
            securityConstraintDescriptor.getAuthConstraints().add(authConstraintDescriptor);
        }
        for (WebResourceCollectionType webResourceCollectionType : securityConstraintType.getWebResourceCollection()) {
            WebResourceCollectionDescriptor webResourceCollectionDescriptor = store.create(WebResourceCollectionDescriptor.class);
            webResourceCollectionDescriptor.setName(webResourceCollectionType.getWebResourceName().getValue());
            for (DescriptionType descriptionType : webResourceCollectionType.getDescription()) {
                webResourceCollectionDescriptor.getDescriptions().add(XmlDescriptorHelper.createDescription(descriptionType, store));
            }
            for (String httpMethod : webResourceCollectionType.getHttpMethod()) {
                HttpMethodDescriptor httpMethodDescriptor = store.create(HttpMethodDescriptor.class);
                httpMethodDescriptor.setName(httpMethod);
                webResourceCollectionDescriptor.getHttpMethods().add(httpMethodDescriptor);
            }
            for (String httpMethodOmission : webResourceCollectionType.getHttpMethodOmission()) {
                HttpMethodOmissionDescriptor httpMethodOmissionDescriptor = store.create(HttpMethodOmissionDescriptor.class);
                httpMethodOmissionDescriptor.setName(httpMethodOmission);
                webResourceCollectionDescriptor.getHttpMethodOmissions().add(httpMethodOmissionDescriptor);
            }
            for (UrlPatternType urlPatternType : webResourceCollectionType.getUrlPattern()) {
                UrlPatternDescriptor urlPatternDescriptor = createUrlPattern(urlPatternType, store);
                webResourceCollectionDescriptor.getUrlPatterns().add(urlPatternDescriptor);
            }
            securityConstraintDescriptor.getWebResourceCollections().add(webResourceCollectionDescriptor);
        }
        return securityConstraintDescriptor;
    }

    private ListenerDescriptor createListener(ListenerType listenerType, ScannerContext context) {
        Store store = context.getStore();
        ListenerDescriptor listenerDescriptor = store.create(ListenerDescriptor.class);
        for (DescriptionType descriptionType : listenerType.getDescription()) {
            listenerDescriptor.getDescriptions().add(XmlDescriptorHelper.createDescription(descriptionType, store));
        }
        for (DisplayNameType displayNameType : listenerType.getDisplayName()) {
            listenerDescriptor.getDisplayNames().add(XmlDescriptorHelper.createDisplayName(displayNameType, store));
        }
        for (IconType iconType : listenerType.getIcon()) {
            listenerDescriptor.getIcons().add(XmlDescriptorHelper.createIcon(iconType, store));
        }
        TypeResolver typeResolver = context.peek(TypeResolver.class);
        FullyQualifiedClassType listenerClass = listenerType.getListenerClass();
        TypeCache.CachedType<TypeDescriptor> listenerClassDescriptor = typeResolver.resolve(listenerClass.getValue(), context);
        listenerDescriptor.setType(listenerClassDescriptor.getTypeDescriptor());
        return listenerDescriptor;
    }

    private ErrorPageDescriptor createErrorPage(ErrorPageType errorPageType, ScannerContext context) {
        ErrorPageDescriptor errorPageDescriptor = context.getStore().create(ErrorPageDescriptor.class);
        ErrorCodeType errorCode = errorPageType.getErrorCode();
        if (errorCode != null) {
            errorPageDescriptor.setErrorCode(errorCode.getValue().intValue());
        }
        FullyQualifiedClassType exceptionType = errorPageType.getExceptionType();
        if (exceptionType != null) {
            TypeResolver typeResolver = context.peek(TypeResolver.class);
            TypeCache.CachedType<TypeDescriptor> cachedType = typeResolver.resolve(exceptionType.getValue(), context);
            errorPageDescriptor.setExceptionType(cachedType.getTypeDescriptor());
        }
        errorPageDescriptor.setErrorPage(errorPageType.getLocation().getValue());
        return errorPageDescriptor;
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
    private FilterDescriptor createFilter(FilterType filterType, Map<String, FilterDescriptor> filters, ScannerContext context) {
        Store store = context.getStore();
        FilterDescriptor filterDescriptor = getOrCreateNamedDescriptor(FilterDescriptor.class, filterType.getFilterName().getValue(), filters, store);
        setAsyncSupported(filterDescriptor, filterType.getAsyncSupported());
        for (DescriptionType descriptionType : filterType.getDescription()) {
            filterDescriptor.getDescriptions().add(XmlDescriptorHelper.createDescription(descriptionType, store));
        }
        for (DisplayNameType displayNameType : filterType.getDisplayName()) {
            filterDescriptor.getDisplayNames().add(XmlDescriptorHelper.createDisplayName(displayNameType, store));
        }
        FullyQualifiedClassType filterClass = filterType.getFilterClass();
        if (filterClass != null) {
            TypeResolver typeResolver = context.peek(TypeResolver.class);
            TypeCache.CachedType<TypeDescriptor> filterClassDescriptor = typeResolver.resolve(filterClass.getValue(), context);
            filterDescriptor.setType(filterClassDescriptor.getTypeDescriptor());
        }
        for (IconType iconType : filterType.getIcon()) {
            IconDescriptor iconDescriptor = XmlDescriptorHelper.createIcon(iconType, store);
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
    private FilterMappingDescriptor createFilterMapping(FilterMappingType filterMappingType, Map<String, FilterDescriptor> filters,
            Map<String, ServletDescriptor> servlets, Store store) {
        FilterMappingDescriptor filterMappingDescriptor = store.create(FilterMappingDescriptor.class);
        FilterNameType filterName = filterMappingType.getFilterName();
        FilterDescriptor filterDescriptor = getOrCreateNamedDescriptor(FilterDescriptor.class, filterName.getValue(), filters, store);
        filterDescriptor.getMappings().add(filterMappingDescriptor);
        for (Object urlPatternOrServletName : filterMappingType.getUrlPatternOrServletName()) {
            if (urlPatternOrServletName instanceof UrlPatternType) {
                UrlPatternType urlPatternType = (UrlPatternType) urlPatternOrServletName;
                UrlPatternDescriptor urlPatternDescriptor = createUrlPattern(urlPatternType, store);
                filterMappingDescriptor.getUrlPatterns().add(urlPatternDescriptor);
            } else if (urlPatternOrServletName instanceof ServletNameType) {
                ServletNameType servletNameType = (ServletNameType) urlPatternOrServletName;
                ServletDescriptor servletDescriptor = getOrCreateNamedDescriptor(ServletDescriptor.class, servletNameType.getValue(), servlets, store);
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

    private UrlPatternDescriptor createUrlPattern(UrlPatternType urlPatternType, Store store) {
        UrlPatternDescriptor urlPatternDescriptor = store.create(UrlPatternDescriptor.class);
        urlPatternDescriptor.setValue(urlPatternType.getValue());
        return urlPatternDescriptor;
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
        ServletDescriptor servletDescriptor = getOrCreateNamedDescriptor(ServletDescriptor.class, servletType.getServletName().getValue(), servlets, store);
        setAsyncSupported(servletDescriptor, servletType.getAsyncSupported());
        for (DescriptionType descriptionType : servletType.getDescription()) {
            servletDescriptor.getDescriptions().add(XmlDescriptorHelper.createDescription(descriptionType, store));
        }
        for (DisplayNameType displayNameType : servletType.getDisplayName()) {
            servletDescriptor.getDisplayNames().add(XmlDescriptorHelper.createDisplayName(displayNameType, store));
        }
        TrueFalseType enabled = servletType.getEnabled();
        if (enabled != null) {
            servletDescriptor.setEnabled(enabled.isValue());
        }
        for (IconType iconType : servletType.getIcon()) {
            IconDescriptor iconDescriptor = XmlDescriptorHelper.createIcon(iconType, store);
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
            servletDescriptor.setLoadOnStartup(loadOnStartup.toUpperCase());
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
                DescriptionDescriptor descriptionDescriptor = XmlDescriptorHelper.createDescription(descriptionType, store);
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
                DescriptionDescriptor descriptionDescriptor = XmlDescriptorHelper.createDescription(descriptionType, store);
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
     * Get or create a named descriptor.
     * 
     * @param type
     *            The descriptor type.
     * @param name
     *            The name.
     * @param descriptors
     *            The map of known named descriptors.
     * @param store
     *            The store.
     * @return The servlet descriptor.
     */
    private <T extends NamedDescriptor> T getOrCreateNamedDescriptor(Class<T> type, String name, Map<String, T> descriptors, Store store) {
        T descriptor = descriptors.get(name);
        if (descriptor == null) {
            descriptor = store.create(type);
            descriptor.setName(name);
            descriptors.put(name, descriptor);
        }
        return descriptor;
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
            DescriptionDescriptor descriptionDescriptor = XmlDescriptorHelper.createDescription(descriptionType, store);
            paramValueDescriptor.getDescriptions().add(descriptionDescriptor);
        }
        paramValueDescriptor.setName(paramValueType.getParamName().getValue());
        XsdStringType paramValue = paramValueType.getParamValue();
        if (paramValue != null) {
            paramValueDescriptor.setValue(paramValue.getValue());
        }
        return paramValueDescriptor;
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
     * Create a servlet mapping descriptor.
     * 
     * @param servletMappingType
     *            The XML servlet mapping type.
     * @param store
     *            The store.
     * @return The servlet mapping descriptor.
     */
    private ServletMappingDescriptor createServletMapping(ServletMappingType servletMappingType, Map<String, ServletDescriptor> servlets, Store store) {
        ServletMappingDescriptor servletMappingDescriptor = store.create(ServletMappingDescriptor.class);
        ServletNameType servletName = servletMappingType.getServletName();
        ServletDescriptor servletDescriptor = getOrCreateNamedDescriptor(ServletDescriptor.class, servletName.getValue(), servlets, store);
        servletDescriptor.getMappings().add(servletMappingDescriptor);
        for (UrlPatternType urlPatternType : servletMappingType.getUrlPattern()) {
            UrlPatternDescriptor urlPatternDescriptor = createUrlPattern(urlPatternType, store);
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
