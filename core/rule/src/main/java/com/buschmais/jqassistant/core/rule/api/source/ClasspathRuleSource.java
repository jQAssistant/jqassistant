package com.buschmais.jqassistant.core.rule.api.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

/**
 * A rule source which is provided from a classpath resource.
 */
public class ClasspathRuleSource extends RuleSource {

    /**
     * The resource path where to load rule files from.
     */
    public static final String RULE_RESOURCE_PATH = "META-INF/jqassistant-rules";

    private final ClassLoader classLoader;
    private final String relativePath;
    private final URL resource;

    public ClasspathRuleSource(ClassLoader classLoader, String relativePath) {
        this.classLoader = classLoader;
        this.relativePath = relativePath;
        String classpathResource = RULE_RESOURCE_PATH + "/" + relativePath;
        this.resource = getClassLoader().getResource(classpathResource);
        if (this.resource == null) {
            throw new IllegalArgumentException("Cannot find rule resource in classpath: " + classpathResource);
        }
    }

    @Override
    public String getId() {
        return resource.toString();
    }

    @Override
    public URL getURL() {
        return resource;
    }

    @Override
    public Optional<File> getDirectory() {
        return Optional.empty();
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.openStream();
    }

    private ClassLoader getClassLoader() {
        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }
}
