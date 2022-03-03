package com.buschmais.jqassistant.commandline;

import java.io.*;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.plugin.PluginResolverFactory;
import com.buschmais.jqassistant.commandline.task.DefaultTaskFactoryImpl;
import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;
import com.buschmais.jqassistant.core.configuration.impl.ConfigurationLoaderImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginClassLoader;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginResolver;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;

import io.smallrye.config.PropertiesConfigSource;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class, i.e. the entry point for the CLI.
 *
 * @author jn4, Kontext E GmbH, 23.01.14
 * @author Dirk Mahler
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String ENV_JQASSISTANT_HOME = "JQASSISTANT_HOME";
    private static final String DIRECTORY_PLUGINS = "plugins";

    private static final String OPTION_HELP = "-help";

    private final TaskFactory taskFactory;

    /**
     * The main method.
     *
     * @param args
     *            The command line arguments.
     * @throws IOException
     *             If an error occurs.
     */
    public static void main(String[] args) {
        try {
            TaskFactory taskFactory = new DefaultTaskFactoryImpl();
            new Main(taskFactory).run(args);
        } catch (CliExecutionException e) {
            String message = getErrorMessage(e);
            LOGGER.error(message);
            System.exit(e.getExitCode());
        }
    }

    /**
     * Constructor.
     *
     * @param taskFactory
     *            The task factory to use.
     */
    public Main(TaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }

    /**
     * Run tasks according to the given arguments.
     *
     * @param args
     *            The arguments.
     * @throws CliExecutionException
     *             If execution fails.
     */
    public void run(String[] args) throws CliExecutionException {
        Options options = gatherOptions(taskFactory);
        CommandLine commandLine = getCommandLine(args, options);
        interpretCommandLine(commandLine, options, taskFactory);
    }

    /**
     * Extract an error message from the given exception and its causes.
     *
     * @param e
     *            The exception.
     * @return The error message.
     */
    private static String getErrorMessage(CliExecutionException e) {
        StringBuffer messageBuilder = new StringBuffer();
        Throwable current = e;
        do {
            messageBuilder.append("-> ");
            messageBuilder.append(current.getMessage());
            current = current.getCause();
        }
        while (current != null);
        return messageBuilder.toString();
    }

    /**
     * Initialize the plugin repository.
     *
     * @param configuration
     *     The {@link CliConfiguration}
     * @return The repository.
     * @throws CliExecutionException
     *     If initialization fails.
     */
    private PluginRepository getPluginRepository(CliConfiguration configuration) throws CliExecutionException {
        // create classloader for the plugins/ directory.
        ClassLoader pluginDirectoryClassLoader = createPluginClassLoader();
        PluginResolverFactory pluginResolverFactory = new PluginResolverFactory();
        PluginResolver pluginResolver = pluginResolverFactory.create(configuration);
        // create plugin classloader using classloader for plugins/ directory as parent, adding plugins to be resolved frol PluginResolver
        PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(pluginDirectoryClassLoader, configuration.plugins());
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
        return new PluginRepositoryImpl(pluginConfigurationReader);
    }

    /**
     * Gather all options which are supported by the task (i.e. including standard and specific options).
     *
     * @return The options.
     */
    private Options gatherOptions(TaskFactory taskFactory) {
        final Options options = new Options();
        gatherTasksOptions(taskFactory, options);
        gatherStandardOptions(options);
        return options;
    }

    /**
     * Gathers the standard options shared by all tasks.
     *
     * @param options
     *            The standard options.
     */
    @SuppressWarnings("static-access")
    private void gatherStandardOptions(final Options options) {
        options.addOption(OptionBuilder.withArgName("p").withDescription(
                "Path to property file; default is jqassistant.properties in the class path").withLongOpt("properties").hasArg().create("p"));
        options.addOption(new Option("help", "print this message"));
    }

    /**
     * Gathers the task specific options for all tasks.
     *
     * @param options
     *            The task specific options.
     */
    private void gatherTasksOptions(TaskFactory taskFactory, Options options) {
        for (Task task : taskFactory.getTasks()) {
            for (Option option : task.getOptions()) {
                options.addOption(option);
            }
        }
    }

    /**
     * Returns a string containing the names of all supported tasks.
     *
     * @return The names of all supported tasks.
     */
    private String gatherTaskNames(TaskFactory taskFactory) {
        final StringBuilder builder = new StringBuilder();
        for (String taskName : taskFactory.getTaskNames()) {
            builder.append("'").append(taskName).append("' ");
        }
        return builder.toString().trim();
    }

    /**
     * Parse the command line and execute the requested task.
     *
     * @param commandLine
     *            The command line.
     * @param options
     *            The known options.
     * @throws CliExecutionException
     *             If an error occurs.
     */
    private void interpretCommandLine(CommandLine commandLine, Options options, TaskFactory taskFactory) throws CliExecutionException {
        if(commandLine.hasOption(OPTION_HELP)) {
            printUsage(options, null);
            System.exit(1);
        }

        List<String> taskNames = commandLine.getArgList();
        if (taskNames.isEmpty()) {
            printUsage(options, "A task must be specified, i.e. one  of " + gatherTaskNames(taskFactory));
            System.exit(1);
        }
        Map<String, String> configurationProperties = new HashMap<>();
        List<Task> tasks = new ArrayList<>();
        for (String taskName : taskNames) {
            Task task = taskFactory.fromName(taskName);
            try {
                task.withStandardOptions(commandLine, configurationProperties);
                task.withOptions(commandLine, configurationProperties);
            } catch (CliConfigurationException e) {
                printUsage(options, e.getMessage());
                System.exit(1);
            }
            if (task == null) {
                printUsage(options, "Unknown task " + taskName);
            }
            tasks.add(task);
        }
        PropertiesConfigSource taskConfigSource = new PropertiesConfigSource(configurationProperties, "TaskConfigSource", 110);
        ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl();
        File workingDirectory = new File(".");
        CliConfiguration configuration = configurationLoader.load(configurationLoader.getDefaultConfigurationDirectory(workingDirectory),
            CliConfiguration.class, taskConfigSource);
        PluginRepository pluginRepository = getPluginRepository(configuration);
        Map<String, Object> properties = readProperties(commandLine);
        executeTasks(tasks, configuration, pluginRepository, properties);
    }

    private void executeTasks(List<Task> tasks, Configuration configuration, PluginRepository pluginRepository,
        Map<String, Object> properties) throws CliExecutionException {
        try {
            pluginRepository.initialize();
            for (Task task : tasks) {
                executeTask(task, configuration, pluginRepository, properties);
            }
        } finally {
            pluginRepository.destroy();
        }
    }

    /**
     * Parse the command line
     *
     * @param args
     *            The arguments.
     * @param options
     *            The known options.
     * @return The command line.
     */
    private CommandLine getCommandLine(String[] args, Options options) {
        final CommandLineParser parser = new BasicParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            printUsage(options, e.getMessage());
            System.exit(1);
        }
        return commandLine;
    }

    /**
     * Executes a task.
     *
     * @param task
     *     The task.
     * @param configuration
     *     The {@link Configuration}-
     * @param properties
     *     The plugin properties
     * @throws IOException
     */
    private void executeTask(Task task, Configuration configuration, PluginRepository pluginRepository,
        Map<String, Object> properties) throws CliExecutionException {
        task.initialize(pluginRepository, properties);
        task.run(configuration);
    }

    /**
     * Read the plugin properties file if specified on the command line or if it exists on the class path.
     *
     * @param commandLine
     *            The command line.
     * @return The plugin properties.
     * @throws CliConfigurationException
     *             If an error occurs.
     */
    private Map<String, Object> readProperties(CommandLine commandLine) throws CliConfigurationException {
        final Properties properties = new Properties();
        InputStream propertiesStream;
        if (commandLine.hasOption("p")) {
            File propertyFile = new File(commandLine.getOptionValue("p"));
            if (!propertyFile.exists()) {
                throw new CliConfigurationException("Property file given by command line does not exist: " + propertyFile.getAbsolutePath());
            }
            try {
                propertiesStream = new FileInputStream(propertyFile);
            } catch (FileNotFoundException e) {
                throw new CliConfigurationException("Cannot open property file.", e);
            }
        } else {
            propertiesStream = Main.class.getResourceAsStream("/jqassistant.properties");
        }
        Map<String, Object> result = new HashMap<>();
        if (propertiesStream != null) {
            try {
                properties.load(propertiesStream);
            } catch (IOException e) {
                throw new CliConfigurationException("Cannot load properties from file.", e);
            }
            for (String name : properties.stringPropertyNames()) {
                result.put(name, properties.getProperty(name));
            }
        }
        return result;
    }

    /**
     * Print usage information.
     *
     * @param options
     *            The known options.
     * @param errorMessage
     *            The error message to append.
     */
    private void printUsage(final Options options, final String errorMessage) {
        if(errorMessage != null) {
            System.out.println("Error: " + errorMessage);
        }
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.printHelp(Main.class.getCanonicalName() + " <task> [options]", options);
        System.out.println("Tasks are: " + gatherTaskNames(taskFactory));
        System.out.println("Example: " + Main.class.getCanonicalName() + " scan -f java:classpath::target/classes java:classpath::target/test-classes");
    }

    /**
     * Determine the JQASSISTANT_HOME directory.
     *
     * @return The directory or `null`.
     */
    private File getHomeDirectory() {
        String dirName = System.getenv(ENV_JQASSISTANT_HOME);
        if (dirName != null) {
            File dir = new File(dirName);
            if (dir.exists()) {
                LOGGER.debug("Using JQASSISTANT_HOME '" + dir.getAbsolutePath() + "'.");
                return dir;
            } else {
                LOGGER.warn("JQASSISTANT_HOME '" + dir.getAbsolutePath() + "' points to a non-existing directory.");
                return null;
            }
        }
        LOGGER.warn("JQASSISTANT_HOME is not set.");
        return null;
    }

    /**
     * Create the class loader to be used for detecting and loading plugins.
     *
     * @return The plugin class loader.
     * @throws com.buschmais.jqassistant.commandline.CliExecutionException
     *             If the plugins cannot be loaded.
     */
    private ClassLoader createPluginClassLoader() throws CliExecutionException {
        ClassLoader parentClassLoader = Task.class.getClassLoader();
        File homeDirectory = getHomeDirectory();
        if (homeDirectory != null) {
            File pluginDirectory = new File(homeDirectory, DIRECTORY_PLUGINS);
            if (pluginDirectory.exists()) {
                final List<URL> urls = new ArrayList<>();
                final Path pluginDirectoryPath = pluginDirectory.toPath();
                SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toFile().getName().endsWith(".jar")) {
                            urls.add(file.toFile().toURI().toURL());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                };
                try {
                    Files.walkFileTree(pluginDirectoryPath, visitor);
                } catch (IOException e) {
                    throw new CliExecutionException("Cannot read plugin directory.", e);
                }
                LOGGER.debug("Using plugin URLs: " + urls);
                return new PluginClassLoader(urls, parentClassLoader);
            }
        }
        return parentClassLoader;
    }
}
