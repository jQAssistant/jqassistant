package com.buschmais.jqassistant.scm.cli;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * The classloader used for loading the plugins.
 * 
 * @author dimahler
 */
public class PluginClassLoader extends URLClassLoader {

    private Collection<URL> urls;

    public PluginClassLoader(Collection<URL> jarFiles, ClassLoader parentClassLoader) {
        super(jarFiles.toArray(new URL[0]), parentClassLoader);
        this.urls = jarFiles;
    }

    @Override
    public String toString() {
        return "PluginClassLoader " + urls;
    }

}
