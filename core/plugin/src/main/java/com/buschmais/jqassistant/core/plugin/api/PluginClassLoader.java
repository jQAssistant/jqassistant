package com.buschmais.jqassistant.core.plugin.api;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import static java.util.Collections.emptyList;

/**
 * The classloader used for loading the plugins.
 */
public class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(ClassLoader parent) {
        this(parent, emptyList());
    }

    public PluginClassLoader(ClassLoader parent, Collection<URL> classpath) {
        super(classpath.toArray(new URL[classpath.size()]), parent);
    }

}
