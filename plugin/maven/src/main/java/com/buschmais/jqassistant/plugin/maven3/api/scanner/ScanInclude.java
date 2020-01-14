package com.buschmais.jqassistant.plugin.maven3.api.scanner;

import java.net.URL;

/**
 * Represents a parameter for an additional directory to include while scanning.
 */
public class ScanInclude {

    /**
     * The path to include in scanning.
     */
    private String path;

    private URL url;

    /**
     * The name of the scope to use, e.g. "java:classpath".
     */
    private String scope;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ScanInclude{" + "path='" + path + '\'' + ", url=" + url + ", scope='" + scope + '\'' + '}';
    }
}
