package com.buschmais.jqassistant.core.plugin.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * The classloader used for loading the plugins.
 *
 * <p>
 * Implementation of a child-first classloader preferring classes/resources from
 * the plugins.
 *
 * @link https://gist.github.com/reda-alaoui/a3030964293268eca48ddc66d8a07d74
 */
public class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(Collection<URL> classpath, ClassLoader parent) {
        super(classpath.toArray(new URL[classpath.size()]), parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException | SecurityException e) {
                c = super.loadClass(name, resolve);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        return url != null ? url : super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> localUrls = findResources(name);
        Enumeration<URL> parentUrls = null;
        final List<URL> urls = new ArrayList<>();
        if (getParent() != null) {
            parentUrls = getParent().getResources(name);
        }
        if (localUrls != null) {
            while (localUrls.hasMoreElements()) {
                urls.add(localUrls.nextElement());
            }
        }
        if (parentUrls != null) {
            while (parentUrls.hasMoreElements()) {
                urls.add(parentUrls.nextElement());
            }
        }
        return new Enumeration<URL>() {
            Iterator<URL> iterator = urls.iterator();

            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            public URL nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
