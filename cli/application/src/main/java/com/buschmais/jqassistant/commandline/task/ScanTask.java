package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ScanTask extends AbstractStoreTask {

    public static final String CMDLINE_OPTION_FILES = "f";
    public static final String CMD_LONG_OPTION_FILES = "files";
    public static final String CMDLINE_OPTION_URLS = "u";
    public static final String CMDLINE_LONG_OPTION_URLS = "urls";

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanTask.class);

    private ScopeHelper scopeHelper = new ScopeHelper(LOGGER);

    private List<ScopeHelper.ScopedResource> files = null;
    private List<ScopeHelper.ScopedResource> urls = null;

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @SuppressWarnings("static-access")
    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        withStore(configuration, store -> {
            ScannerContext scannerContext = new ScannerContextImpl(pluginRepository.getClassLoader(), store, new File(DEFAULT_OUTPUT_DIRECTORY));
            if (configuration.scan()
                .reset()) {
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
                    scan(configuration, scannerContext, file, file.getAbsolutePath(), scopeName);
                }
            }
            for (ScopeHelper.ScopedResource scopedResource : urls) {
                String uri = scopedResource.getResource();
                String scopeName = scopedResource.getScopeName();
                try {
                    scan(configuration, scannerContext, new URI(uri), uri, scopeName);
                } catch (URISyntaxException e) {
                    throw new CliConfigurationException("Cannot parse URI " + uri, e);
                }
            }
        });
    }

    private <T> void scan(CliConfiguration configuration, ScannerContext scannerContext, T element, String path, String scopeName) {
        Scanner scanner = new ScannerImpl(configuration.scan(), scannerContext, pluginRepository.getScannerPluginRepository());
        Scope scope = scanner.resolveScope(scopeName);
        scanner.scan(element, path, scope);
    }

    @Override
    public void configure(CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        super.configure(options, configurationBuilder);
        files = scopeHelper.getScopedResources(getOptionValues(options, CMDLINE_OPTION_FILES, emptyList()));
        urls = scopeHelper.getScopedResources(getOptionValues(options, CMDLINE_OPTION_URLS, emptyList()));
        if (files.isEmpty() && urls.isEmpty()) {
            throw new CliConfigurationException("No files, directories or urls given.");
        }
    }

    @Override
    public void addTaskOptions(final List<Option> options) {
        super.addTaskOptions(options);
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_FILES)
            .withLongOpt(CMD_LONG_OPTION_FILES)
            .withDescription("The files or directories to be scanned, comma separated, each with optional scope prefix.")
            .withValueSeparator(',')
            .hasArgs()
            .create(CMDLINE_OPTION_FILES));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_URLS)
            .withLongOpt(CMDLINE_LONG_OPTION_URLS)
            .withDescription("The URIs to be scanned, comma separated, each with optional scope prefix.")
            .withValueSeparator(',')
            .hasArgs()
            .create(CMDLINE_OPTION_URLS));
    }
}
