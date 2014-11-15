package com.buschmais.jqassistant.scm.cli.task;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolverBuilder;
import com.buschmais.jqassistant.scm.cli.CliConfigurationException;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ScanTask extends AbstractJQATask {

    public static final String CMDLINE_OPTION_FILES = "f";
    public static final String CMDLINE_OPTION_URLS = "u";
    public static final String CMDLINE_OPTION_RESET = "reset";
    private List<String> fileNames = new ArrayList<>();
    private List<String> urls = new ArrayList<>();
    private boolean reset = false;

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     */
    public ScanTask(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        List<ScannerPlugin<?, ?>> scannerPlugins;
        try {
            scannerPlugins = scannerPluginRepository.getScannerPlugins();
        } catch (PluginRepositoryException e) {
            throw new RuntimeException(e);
        }
        if (reset) {
            store.reset();
        }
        properties = new HashMap<>();
        for (String fileName : fileNames) {
            final File file = new File(fileName);
            String absolutePath = file.getAbsolutePath();
            if (!file.exists()) {
                getLog().info(absolutePath + "' does not exist, skipping scan.");
            } else {
                scan(store, file, file.getAbsolutePath(), scannerPlugins);
            }
        }
        for (String url : urls) {
            try {
                scan(store, new URL(url), url, scannerPlugins);
            } catch (MalformedURLException e) {
                throw new CliConfigurationException("Cannot parse URL " + url, e);
            }
        }
    }

    private <T> void scan(Store store, T element, String path, List<ScannerPlugin<?, ?>> scannerPlugins) {
        store.beginTransaction();
        Scanner scanner = new ScannerImpl(store, scannerPlugins);
        ScannerContext context = scanner.getContext();
        context.push(TypeResolver.class, TypeResolverBuilder.createTypeResolver(context));
        try {
            scanner.scan(element, path, CLASSPATH);
        } finally {
            context.pop(TypeResolver.class);
            store.commitTransaction();
        }
    }

    @Override
    public void withOptions(final CommandLine options) throws CliConfigurationException {
        fileNames = getOptionValues(options, CMDLINE_OPTION_FILES, Collections.<String> emptyList());
        urls = getOptionValues(options, CMDLINE_OPTION_URLS, Collections.<String> emptyList());
        if (fileNames.isEmpty() && urls.isEmpty()) {
            throw new CliConfigurationException("No files, directories or urls given.");
        }
        reset = options.hasOption(CMDLINE_OPTION_RESET);
    }

    @SuppressWarnings("static-access")
    @Override
    protected void addTaskOptions(final List<Option> options) {
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_FILES).withLongOpt("files")
                .withDescription("The files or directories to be scanned, comma separated.").withValueSeparator(',').hasArgs().create(CMDLINE_OPTION_FILES));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_URLS).withLongOpt("urls").withDescription("The URLs to be scanned, comma separated.")
                .withValueSeparator(',').hasArgs().create(CMDLINE_OPTION_URLS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RESET).withDescription("Reset store before scanning (default=false).")
                .create(CMDLINE_OPTION_RESET));
    }
}
