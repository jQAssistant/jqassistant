package com.buschmais.jqassistant.core.plugin.api;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * The classloader used for loading the plugins.
 */
public class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(Collection<URL> classpath, ClassLoader parent) {
        super(classpath.toArray(new URL[classpath.size()]), parent);
    }

}
