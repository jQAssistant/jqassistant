package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;
import com.buschmais.jqassistant.core.scanner.api.configuration.Include;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ScanTask extends AbstractStoreTask {

    public static final String CMDLINE_OPTION_FILES = "f";
    public static final String CMD_LONG_OPTION_FILES = "files";
    public static final String CMDLINE_OPTION_URLS = "u";
    public static final String CMDLINE_LONG_OPTION_URLS = "urls";

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanTask.class);

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        withStore(configuration, store -> {
            ScannerContext scannerContext = new ScannerContextImpl(pluginRepository.getClassLoader(), store, new File(DEFAULT_WORKING_DIRECTORY),
                new File(DEFAULT_OUTPUT_DIRECTORY));
            if (configuration.scan()
                .reset().orElse(false)) {
                store.reset();
            }
            configuration.scan()
                .include()
                .ifPresentOrElse(include -> {
                    scanFiles(configuration, include.files(), scannerContext);
                    scanUris(configuration, include.urls(), scannerContext);
                }, () -> LOGGER.warn("No files, directories or urls given."));
        });
    }

    private void scanUris(CliConfiguration configuration, Optional<List<String>> urlsOptional, ScannerContext scannerContext) {
        urlsOptional.ifPresent(urls -> {
            ScopeHelper scopeHelper = new ScopeHelper(log);
            for (ScopeHelper.ScopedResource scopedResource: scopeHelper.getScopedResources(urls)) {
                String uri = scopedResource.getResource();
                String scopeName = scopedResource.getScopeName();
                try {
                    scan(configuration, scannerContext, new URI(uri), uri, scopeName);
                } catch (URISyntaxException e) {
                    throw new IllegalStateException("Cannot parse URI " + uri, e);
                }
            }
        });
    }

    private void scanFiles(CliConfiguration configuration, Optional<List<String>> filesOptional, ScannerContext scannerContext) {
        filesOptional.ifPresent(files -> {
            ScopeHelper scopeHelper = new ScopeHelper(log);
            for (ScopeHelper.ScopedResource scopedResource : scopeHelper.getScopedResources(files)) {
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
        });
    }

    private <T> void scan(CliConfiguration configuration, ScannerContext scannerContext, T element, String path, String scopeName) {
        Scanner scanner = new ScannerImpl(configuration.scan(), scannerContext, pluginRepository.getScannerPluginRepository());
        Scope scope = scanner.resolveScope(scopeName);
        log.info("Scanning '{}'.", path);
        scanner.scan(element, path, scope);
    }

    @Override
    public void configure(CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        super.configure(options, configurationBuilder);
        List<String> files = getOptionValues(options, CMDLINE_OPTION_FILES, emptyList());
        List<String> urls = getOptionValues(options, CMDLINE_OPTION_URLS, emptyList());
        configurationBuilder.with(Include.class, Include.FILES, files);
        configurationBuilder.with(Include.class, Include.URLS, urls);
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
