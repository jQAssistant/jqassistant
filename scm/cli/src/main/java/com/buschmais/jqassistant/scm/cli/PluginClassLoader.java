package com.buschmais.jqassistant.scm.cli;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The classloader used for loading the plugins.
 * 
 * @author dimahler
 */
public class PluginClassLoader extends URLClassLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginClassLoader.class);

    private Collection<URL> urls;

    public PluginClassLoader(Collection<URL> jarFiles, ClassLoader parentClassLoader) {
        super(jarFiles.toArray(new URL[0]), parentClassLoader);
        this.urls = jarFiles;
    }

    @Override
    protected PermissionCollection getPermissions(CodeSource codesource) {
        LOGGER.info("Getting permissions for " + codesource.getLocation());
        return new Permissions();
    }

    @Override
    public String toString() {
        return "PluginClassLoader " + urls;
    }

}
