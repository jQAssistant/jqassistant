package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ScanTask extends AbstractStoreTask {

    public static final String CMDLINE_OPTION_FILES = "f";
    public static final String CMD_LONG_OPTION_FILES = "files";
    public static final String CMDLINE_OPTION_URLS = "u";
    public static final String CMDLINE_LONG_OPTION_URLS = "urls";
    public static final String CMDLINE_OPTION_RESET = "reset";
    public static final String CMDLINE_OPTION_CONTINUEONERROR = "continueOnError";

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanTask.class);

    private ScopeHelper scopeHelper = new ScopeHelper(LOGGER);

    private List<ScopeHelper.ScopedResource> files = null;
    private List<ScopeHelper.ScopedResource> urls = null;
    private boolean reset = false;
    private boolean continueOnError = false;

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @SuppressWarnings("static-access")
    @Override
    public void addTaskOptions(final List<Option> options) {
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_FILES).withLongOpt(CMD_LONG_OPTION_FILES)
                .withDescription("The files or directories to be scanned, comma separated, each with optional scope prefix.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_FILES));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_URLS).withLongOpt(CMDLINE_LONG_OPTION_URLS)
                .withDescription("The URIs to be scanned, comma separated, each with optional scope prefix.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_URLS));
        options.add(
                OptionBuilder.withArgName(CMDLINE_OPTION_RESET).withDescription("Reset store before scanning (default=false).").create(CMDLINE_OPTION_RESET));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONTINUEONERROR).withDescription("Continue scanning if an error is encountered. (default=false).")
                .create(CMDLINE_OPTION_CONTINUEONERROR));
    }

    @Override
    protected void executeTask(Store store) throws CliExecutionException {
        ScannerContext scannerContext = new ScannerContextImpl(store);
        if (reset) {
            store.reset();
        }
        for (ScopeHelper.ScopedResource scopedResource : files) {
            String fileName = scopedResource.getResource();
            String scopeName = scopedResource.getScopeName();
            File file = new File(fileName);
            String absolutePath = file.getAbsolutePath();
            if (!file.exists()) {
                LOGGER.info(absolutePath + "' does not exist, skipping scan.");
            } else {
                scan(scannerContext, file, file.getAbsolutePath(), scopeName);
            }
        }
        for (ScopeHelper.ScopedResource scopedResource : urls) {
            String uri = scopedResource.getResource();
            String scopeName = scopedResource.getScopeName();
            try {
                scan(scannerContext, new URI(uri), uri, scopeName);
            } catch (URISyntaxException e) {
                throw new CliConfigurationException("Cannot parse URI " + uri, e);
            }
        }
    }

    private <T> void scan(ScannerContext scannerContext, T element, String path, String scopeName) {
        ScannerConfiguration configuration = new ScannerConfiguration();
        configuration.setContinueOnError(continueOnError);
        Scanner scanner = new ScannerImpl(configuration, pluginProperties, scannerContext, pluginRepository.getScannerPluginRepository());
        Scope scope = scanner.resolveScope(scopeName);
        scanner.scan(element, path, scope);
    }

    @Override
    public void withOptions(final CommandLine options) throws CliConfigurationException {
        files = scopeHelper.getScopedResources(getOptionValues(options, CMDLINE_OPTION_FILES, Collections.emptyList()));
        urls = scopeHelper.getScopedResources(getOptionValues(options, CMDLINE_OPTION_URLS, Collections.<String> emptyList()));
        if (files.isEmpty() && urls.isEmpty()) {
            throw new CliConfigurationException("No files, directories or urls given.");
        }
        reset = options.hasOption(CMDLINE_OPTION_RESET);
        continueOnError = options.hasOption(CMDLINE_OPTION_CONTINUEONERROR);
    }
}
