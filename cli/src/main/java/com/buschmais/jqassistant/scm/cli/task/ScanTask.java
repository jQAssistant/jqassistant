package com.buschmais.jqassistant.scm.cli.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliConfigurationException;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ScanTask extends AbstractTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScanTask.class);

    public static final String CMDLINE_OPTION_FILES = "f";
    public static final String CMDLINE_OPTION_URIS = "u";
    public static final String CMDLINE_OPTION_RESET = "reset";
    private Map<String, String> files = Collections.emptyMap();
    private Map<String, String> urls = Collections.emptyMap();
    private boolean reset = false;

    @SuppressWarnings("static-access")
    @Override
    protected void addTaskOptions(final List<Option> options) {
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_FILES).withLongOpt("files")
                .withDescription("The files or directories to be scanned, comma separated, each with optional scope prefix.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_FILES));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_URIS).withLongOpt("uris")
                .withDescription("The URIs to be scanned, comma separated, each with optional scope prefix.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_URIS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RESET).withDescription("Reset store before scanning (default=false).")
                .create(CMDLINE_OPTION_RESET));
    }

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        ScannerContext scannerContext = new ScannerContextImpl(store);
        List<ScannerPlugin<?, ?>> scannerPlugins;
        try {
            scannerPlugins = pluginRepository.getScannerPluginRepository().getScannerPlugins(scannerContext, pluginProperties);
        } catch (PluginRepositoryException e) {
            throw new CliExecutionException("Cannot get scanner plugins.", e);
        }
        if (reset) {
            store.reset();
        }
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String fileName = entry.getKey();
            String scopeName = entry.getValue();
            final File file = new File(fileName);
            String absolutePath = file.getAbsolutePath();
            if (!file.exists()) {
                LOGGER.info(absolutePath + "' does not exist, skipping scan.");
            } else {
                scan(scannerContext, file, file.getAbsolutePath(), scopeName, scannerPlugins);
            }
        }
        for (Map.Entry<String, String> entry : urls.entrySet()) {
            String uri = entry.getKey();
            String scopeName = entry.getValue();
            try {
                scan(scannerContext, new URI(uri), uri, scopeName, scannerPlugins);
            } catch (URISyntaxException e) {
                throw new CliConfigurationException("Cannot parse URI " + uri, e);
            }
        }
    }

    /**
     * Parses the given list of option values into a map of resources and their
     * associated (optional) scopes.
     *
     * Example: "maven:repository::http://my-host/repo" will be an entry with
     * key "maven:repository" and value "http://my-host/repo".
     *
     * @param optionValues
     *            The value.
     * @return The map of resources and scopes.
     */
    private Map<String, String> parseResources(List<String> optionValues) {
        Map<String, String> resources = new HashMap<>();
        for (String file : optionValues) {
            String[] parts = file.split("::");
            String fileName;
            String scopeName = null;
            if (parts.length == 2) {
                scopeName = parts[0];
                fileName = parts[1];
            } else {
                fileName = parts[0];
            }
            resources.put(fileName, scopeName);
        }
        return resources;
    }

    private <T> void scan(ScannerContext scannerContext, T element, String path, String scopeName, List<ScannerPlugin<?, ?>> scannerPlugins) throws CliExecutionException {
        Store store = scannerContext.getStore();
        store.beginTransaction();
        Scanner scanner;
        try {
            scanner = new ScannerImpl(scannerContext, scannerPlugins, pluginRepository.getScopePluginRepository().getScopes());
        } catch (PluginRepositoryException e) {
            throw new CliExecutionException("Cannot get scope plugins.", e);
        }
        Scope scope = scanner.resolveScope(scopeName);
        try {
            scanner.scan(element, path, scope);
        } finally {
            store.commitTransaction();
        }
    }

    @Override
    public void withOptions(final CommandLine options) throws CliConfigurationException {
        files = parseResources(getOptionValues(options, CMDLINE_OPTION_FILES, Collections.<String> emptyList()));
        urls = parseResources(getOptionValues(options, CMDLINE_OPTION_URIS, Collections.<String> emptyList()));
        if (files.isEmpty() && urls.isEmpty()) {
            throw new CliConfigurationException("No files, directories or urls given.");
        }
        reset = options.hasOption(CMDLINE_OPTION_RESET);
    }
}
