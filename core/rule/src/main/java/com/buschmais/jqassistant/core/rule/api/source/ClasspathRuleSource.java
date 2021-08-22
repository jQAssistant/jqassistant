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
    private final String resource;
    private final String relativePath;

    public ClasspathRuleSource(ClassLoader classLoader, String relativePath) {
        this.classLoader = classLoader;
        this.relativePath = relativePath;
        this.resource = RULE_RESOURCE_PATH + "/" + relativePath;

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
    public Optional<File> getDirectory() {
        return Optional.empty();
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        ClassLoader currentClassloader = getClassLoader();
        InputStream stream = currentClassloader.getResourceAsStream(resource);
        if (stream == null) {
            throw new IOException("Cannot load resource from " + resource);
        }
        return stream;
    }

    private ClassLoader getClassLoader() {
        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }
}
