package com.buschmais.jqassistant.core.rule.api.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * A rule source which is provided from a classpath resource.
 */
@Slf4j
public class ClasspathRuleSource extends RuleSource {

    /**
     * The resource path where to load rule files from.
     */
    public static final String RULE_RESOURCE_PATH = "META-INF/jqassistant-rules";

    private final String relativePath;
    private final URL resource;

    public ClasspathRuleSource(URL pluginBaseUrl, String relativePath) {
        this.relativePath = relativePath;
        this.resource = getResource(pluginBaseUrl, relativePath);
    }

    private URL getResource(URL pluginBaseUrl, String relativePath) {
        try {
            URL url = new URL(pluginBaseUrl.toString() + RULE_RESOURCE_PATH + "/" + relativePath);
            log.debug("Resolved rule resource from plugin base url '{}' and relative path '{}' to '{}'.", pluginBaseUrl, relativePath, url);
            return url;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot resolve rule resource from plugin classpath: " + relativePath, e);
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
        InputStream inputStream = resource.openStream();
        if (inputStream == null) {
            throw new IOException("Cannot open rule resource: " + resource);
        }
        return inputStream;
    }
}
