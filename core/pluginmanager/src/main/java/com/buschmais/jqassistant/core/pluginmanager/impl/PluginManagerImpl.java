package com.buschmais.jqassistant.core.pluginmanager.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.impl.XmlHelper;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ObjectFactory;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ResourcesType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.RulesType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ScannerType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.StoreType;
import com.buschmais.jqassistant.core.pluginmanager.api.PluginManager;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;

/**
 * Plugin reader implementation.
 */
public class PluginManagerImpl implements PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManagerImpl.class);

    private JAXBContext jaxbContext;

    private List<JqassistantPlugin> plugins;

    /**
     * Constructor.
     */
    public PluginManagerImpl() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    /**
     * Read the catalogs from an {@link URL}.
     *
     * @param pluginUrl The {@link URL}.
     * @return The {@link JqassistantPlugin}.
     */
    private JqassistantPlugin readPlugin(URL pluginUrl) {
        InputStream inputStream;
        try {
            inputStream = pluginUrl.openStream();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open plugin stream.", e);
        }
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(XmlHelper.getSchema(PLUGIN_SCHEMA_RESOURCE));
            return unmarshaller.unmarshal(new StreamSource(inputStream), JqassistantPlugin.class).getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot read plugin from " + pluginUrl.toString(), e);
        }
    }

    @Override
    public List<Source> getRuleSources() {
        List<Source> sources = new ArrayList<>();
        for (JqassistantPlugin plugin : getPlugins()) {
            RulesType rulesType = plugin.getRules();
            if (rulesType != null) {
                String directory = rulesType.getDirectory();
                for (ResourcesType resourcesType : rulesType.getResources()) {
                    for (String resource : resourcesType.getResource()) {
                        StringBuffer fullResource = new StringBuffer();
                        if (directory != null) {
                            fullResource.append(directory);
                        }
                        fullResource.append(resource);
                        URL url = PluginManagerImpl.class.getResource(fullResource.toString());
                        String systemId = null;
                        if (url != null) {
                            try {
                                systemId = url.toURI().toString();
                                if (LOGGER.isDebugEnabled()) {
									LOGGER.debug("Adding rulesType from " + url.toString());
								}
                                InputStream ruleStream = url.openStream();
                                sources.add(new StreamSource(ruleStream, systemId));
                            } catch (IOException e) {
                                throw new IllegalStateException("Cannot open rules URL: " + url.toString(), e);
                            } catch (URISyntaxException e) {
                                throw new IllegalStateException("Cannot create URI from url: " + url.toString());
                            }
                        } else {
                            LOGGER.warn("Cannot read rulesType from resource '{}'" + resource);
                        }
                    }
                }
            }
        }
        return sources;
    }

    @Override
    public List<Class<?>> getDescriptorTypes() throws PluginReaderException {
        List<Class<?>> types = new ArrayList<>();
        for (JqassistantPlugin plugin : getPlugins()) {
            StoreType storeType = plugin.getStore();
            if (storeType != null) {
                for (String typeName : storeType.getType()) {
                    types.add(getType(typeName));
                }
            }
        }
        return types;
    }

    @Override
    public List<FileScannerPlugin<?>> getScannerPlugins() throws PluginReaderException {
        List<FileScannerPlugin<?>> fileScannerPlugins = new ArrayList<>();
        for (JqassistantPlugin plugin : getPlugins()) {
            ScannerType scannerType = plugin.getScanner();
            if (scannerType != null) {
                for (String scannerPluginName : scannerType.getPlugin()) {
					FileScannerPlugin<?> scannerPlugin = createInstance(FileScannerPlugin.class, scannerPluginName);
					if (scannerPlugin != null) {
						fileScannerPlugins.add(scannerPlugin);
					}
                }
            }
        }
        return fileScannerPlugins;
    }

	@Override
	public List<ProjectScannerPlugin<?>> getProjectScannerPlugins() throws PluginReaderException {
		List<ProjectScannerPlugin<?>> projectScannerPlugins = new ArrayList<>();
		for (JqassistantPlugin plugin : getPlugins()) {
			ScannerType scannerType = plugin.getScanner();
			if (scannerType != null) {
				for (String scannerPluginName : scannerType.getPlugin()) {
					ProjectScannerPlugin<?> scannerPlugin = createInstance(ProjectScannerPlugin.class, scannerPluginName);
					if (scannerPlugin != null) {
						projectScannerPlugins.add(scannerPlugin);
					}
				}
			}
		}
		return projectScannerPlugins;
	}

    /**
     * Returns an {@link Iterable} over all plugins which can be resolved from
     * the current classpath.
     *
     * @return The plugins which can be resolved from the current classpath.
     */
    private List<JqassistantPlugin> getPlugins() {
        if (this.plugins == null) {
            final Enumeration<URL> resources;
            try {
                resources = PluginManagerImpl.class.getClassLoader().getResources(PLUGIN_RESOURCE);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot get plugin resources.", e);
            }
            this.plugins = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Reading plugin descriptor from URL '{}'.", url);
				}
                this.plugins.add(readPlugin(url));
            }
        }
        return this.plugins;
    }

    /**
     * Create and return an instance of the given type name.
     *
     * @param typeName The type name.
     * @param <T>      The type.
     * @return The instance.
     * @throws PluginReaderException If the instance cannot be created.
     */
    private <T> Class<T> getType(String typeName) throws PluginReaderException {
        Class<T> type;
        try {
            return (Class<T>) Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new PluginReaderException("Cannot find class " + typeName, e);
        }
    }

	private <T> T createInstance(Class<? extends ScannerPlugin> clazz, String typeName) throws PluginReaderException {
        Class<T> type = getType(typeName);
        try {
			T newInstance = type.newInstance();
			return clazz.isInstance(newInstance) ? newInstance : null;
        } catch (InstantiationException e) {
            throw new PluginReaderException("Cannot create instance of class " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new PluginReaderException("Cannot access class " + typeName, e);
        }
    }
}
