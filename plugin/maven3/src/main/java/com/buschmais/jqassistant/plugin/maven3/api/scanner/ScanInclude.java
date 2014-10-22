package com.buschmais.jqassistant.plugin.maven3.api.scanner;

/**
 * Represents a parameter for an additional directory to include while scanning.
 */
public class ScanInclude {

    /**
     * The path to include in scanning.
     */
    private String path;

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

    @Override
    public String toString() {
        return "ScanInclude{" + "path='" + path + '\'' + ", scope='" + scope + '\'' + '}';
    }
}
