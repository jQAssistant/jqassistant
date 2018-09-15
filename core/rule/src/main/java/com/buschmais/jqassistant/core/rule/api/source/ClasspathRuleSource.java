package com.buschmais.jqassistant.core.rule.api.source;

import java.io.InputStream;
import java.net.URL;

/**
 * A rule source which is provided from a classpath resource.
 */
public class ClasspathRuleSource extends RuleSource {

    private ClassLoader classLoader;
    private String resource;

    public ClasspathRuleSource(ClassLoader classLoader, String resource) {
        this.classLoader = classLoader;
        this.resource = resource;
    }

    @Override
    public String getId() {
        return resource;
    }

    @Override
    public URL getURL() {
        return getClassLoader().getResource(resource);
    }

    @Override
    public InputStream getInputStream() {
        ClassLoader currentClassloader = getClassLoader();
        return currentClassloader.getResourceAsStream(resource);
    }

    private ClassLoader getClassLoader() {
        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }
}
