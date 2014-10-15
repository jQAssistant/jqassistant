package com.buschmais.jqassistant.scm.cli;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ScanTask extends AbstractJQATask implements OptionsConsumer {

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
     *            The
     *            {@link com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader}
     *            .
     */
    public ScanTask(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

    @Override
    protected void executeTask(final Store store) {
        List<ScannerPlugin<?>> scannerPlugins;
        try {
            scannerPlugins = getScannerPluginRepository(properties).getScannerPlugins();
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
                throw new IllegalArgumentException("Cannot parse URL " + url, e);
            }
        }
    }

    protected ScannerPluginRepository getScannerPluginRepository(Map<String, Object> properties) {
        try {
            return new ScannerPluginRepositoryImpl(pluginConfigurationReader, properties);
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot create scanner plugin repository.", e);
        }
    }

    private <T> void scan(Store store, T element, String path, List<ScannerPlugin<?>> scannerPlugins) {
        store.beginTransaction();
        try {
            Scanner scanner = new ScannerImpl(store, scannerPlugins);
            scanner.scan(element, path, CLASSPATH);
        } finally {
            store.commitTransaction();
        }
    }

    @Override
    public void withOptions(final CommandLine options) {
        fileNames = getOptionValues(options, CMDLINE_OPTION_FILES, Collections.<String> emptyList());
        urls = getOptionValues(options, CMDLINE_OPTION_URLS, Collections.<String> emptyList());
        if (fileNames.isEmpty() && urls.isEmpty()) {
            throw new MissingConfigurationParameterException("No files, directories or urls given.");
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
