package com.buschmais.jqassistant.scm.cli;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.analysis.api.Console;
import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.*;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.common.report.ReportHelper;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public abstract class AbstractJQATask implements JQATask {

    protected static final String CMDLINE_OPTION_S = "s";

    protected static final String CMDLINE_OPTION_REPORTDIR = "reportDirectory";

    private static final String ENV_JQASSISTANT_HOME = "JQASSISTANT_HOME";
    private static final String DIRECTORY_PLUGINS = "plugins";
    private static final Console LOG = Log.getLog();
    private static final File HOME_DIRECTORY = getHomeDirectory();

    private static final PluginConfigurationReader PLUGIN_CONFIGURATION_READER = new PluginConfigurationReaderImpl(createPluginClassLoader());

    protected Map<String, Object> properties = new HashMap<>();
    protected String storeDirectory;
    protected ReportHelper reportHelper;
    protected ClassLoader classLoader;
    protected ModelPluginRepository modelPluginRepository;
    protected ScannerPluginRepository scannerPluginRepository;
    protected RulePluginRepository rulePluginRepository;
    protected ReportPluginRepository reportPluginRepository;

    /**
     * Determine the JQASSISTANT_HOME directory.
     *
     * @return The directory or <code>null</code>.
     */
    private static File getHomeDirectory() {
        String dirName = System.getenv(ENV_JQASSISTANT_HOME);
        if (dirName != null) {
            File dir = new File(dirName);
            if (dir.exists()) {
                LOG.info("Using JQASSISTANT_HOME '" + dir.getAbsolutePath() + "'.");
                return dir;
            } else {
                LOG.warn("JQASSISTANT_HOME '" + dir.getAbsolutePath() + "' points to a non-existing directory.");
                return null;
            }
        }
        LOG.warn("JQASSISTANT_HOME is not set.");
        return null;
    }

    /**
     * Create the class loader to be used for detecting and loading plugins.
     *
     * @return The plugin class loader.
     */
    private static ClassLoader createPluginClassLoader() {
        ClassLoader parentClassLoader = JQATask.class.getClassLoader();
        if (HOME_DIRECTORY != null) {
            File pluginDirectory = new File(HOME_DIRECTORY, DIRECTORY_PLUGINS);
            if (pluginDirectory.exists()) {
                final Path pluginDirectoryPath = pluginDirectory.toPath();
                final List<URL> files = new ArrayList<>();
                SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toFile().getName().endsWith(".jar")) {
                            files.add(file.toFile().toURI().toURL());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                };
                try {
                    Files.walkFileTree(pluginDirectoryPath, visitor);
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot read plugin directory.", e);
                }
                return new URLClassLoader(files.toArray(new URL[0]), parentClassLoader);
            }
        }
        return parentClassLoader;
    }

    /**
     * Constructor.
     */
    protected AbstractJQATask() {
        try {
            classLoader = PLUGIN_CONFIGURATION_READER.getClassLoader();
            modelPluginRepository = new ModelPluginRepositoryImpl(PLUGIN_CONFIGURATION_READER);
            scannerPluginRepository = new ScannerPluginRepositoryImpl(PLUGIN_CONFIGURATION_READER, properties);
            rulePluginRepository = new RulePluginRepositoryImpl(PLUGIN_CONFIGURATION_READER);
            reportPluginRepository = new ReportPluginRepositoryImpl(PLUGIN_CONFIGURATION_READER, properties);
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannpt create plugin repositories.", e);
        }
        this.reportHelper = new ReportHelper(Log.getLog());
    }

    @Override
    public void initialize(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void run() {
        List<Class<?>> descriptorTypes;
        final Store store = getStore();
        try {
            descriptorTypes = modelPluginRepository.getDescriptorTypes();
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot get model.", e);
        }
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            store.start(descriptorTypes);
            executeTask(store);
        } finally {
            store.stop();
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    @Override
    public void withStandardOptions(CommandLine options) {
        storeDirectory = getOptionValue(options, CMDLINE_OPTION_S, DEFAULT_STORE_DIRECTORY);
    }

    @Override
    public List<Option> getOptions() {
        final List<Option> options = new ArrayList<>();
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_S).withLongOpt("storeDirectory").withDescription("The location of the Neo4j database.").hasArgs()
                .create(CMDLINE_OPTION_S));
        addTaskOptions(options);
        return options;
    }

    protected List<String> getOptionValues(CommandLine options, String option, List<String> defaultValues) {
        if (options.hasOption(option)) {
            List<String> names = new ArrayList<>();
            for (String elementName : options.getOptionValues(option)) {
                if (elementName.trim().length() > 0)
                    names.add(elementName);
            }
            return names;
        }
        return defaultValues;
    }

    protected String getOptionValue(CommandLine options, String option, String defaultValue) {
        if (options.hasOption(option)) {
            return options.getOptionValue(option);
        } else {
            return defaultValue;
        }
    }

    protected void addTaskOptions(final List<Option> options) {
    }

    /**
     * Return the {@link Store} instance.
     *
     * @return The store.
     */
    protected Store getStore() {
        File directory = new File(storeDirectory);
        LOG.info("Opening store in directory '" + directory.getAbsolutePath() + "'");
        directory.getParentFile().mkdirs();
        return new EmbeddedGraphStore(directory.getAbsolutePath());
    }

    protected abstract void executeTask(final Store store);
}
